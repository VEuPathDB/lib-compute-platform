package org.veupathdb.lib.compute.platform.intern.queues

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.JobManager
import org.veupathdb.lib.compute.platform.config.AsyncQueueConfig
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecContext
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecutors
import org.veupathdb.lib.compute.platform.intern.metrics.JobMetrics
import org.veupathdb.lib.compute.platform.intern.metrics.QueueMetrics
import org.veupathdb.lib.compute.platform.job.PlatformJobResultStatus
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.rabbit.jobs.JobQueueDispatcher
import org.veupathdb.lib.rabbit.jobs.JobQueueExecutor
import org.veupathdb.lib.rabbit.jobs.config.QueueConfig
import org.veupathdb.lib.rabbit.jobs.model.ErrorNotification
import org.veupathdb.lib.rabbit.jobs.model.JobDispatch
import org.veupathdb.lib.rabbit.jobs.model.SuccessNotification
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/**
 * Queue Wrapper
 *
 * Wraps both the dispatch and worker ends of a queue to execute jobs and handle
 * their results.
 */
internal class QueueWrapper(conf: AsyncQueueConfig) {

  private val Log = LoggerFactory.getLogger(this::class.java)

  val name = conf.id

  private val dispatch: JobQueueDispatcher
  private val handler: JobQueueExecutor

  init {
    Log.info("initializing queue wrapper for queue {}", name)

    val qc = QueueConfig()
      .connection {
        hostname = conf.host
        hostPort = conf.port
        username = conf.username
        password = conf.password
      }
      .executor {
        workers  = conf.workers
        maxJobExecutionTime = conf.messageAckTimeout
      }
      .apply {
        jobQueueName = "${conf.id}_jobs"
        successQueueName = "${conf.id}_success"
        errorQueueName = "${conf.id}_error"
      }

    // Setup dispatch end of queue the wrapper
    dispatch = JobQueueDispatcher(qc)
    dispatch.onError(this::onError)
    dispatch.onSuccess(this::onSuccess)

    // Setup worker end of the queue wrapper
    handler  = JobQueueExecutor(qc)
    handler.onJob(this::onJob)
  }

  fun submitJob(jobID: HashID, config: JsonNode? = null) {
    Log.info("submitting job {} to queue {}", jobID, name)
    QueueMetrics.Queued.labels(name).inc()
    dispatch.dispatch(JobDispatch(jobID, config))
  }

  private fun onError(job: ErrorNotification) {
    Log.info("job {} failed", job.jobID)
    JobMetrics.Failures.labels(name).inc()
    JobManager.setJobFailed(job.jobID)
  }

  private fun onSuccess(job: SuccessNotification) {
    Log.info("job {} succeeded", job.jobID)
    JobMetrics.Successes.labels(name).inc()
    JobManager.setJobComplete(job.jobID)
  }

  private fun onJob(job: JobDispatch) {
    Log.debug("handling job {}", job.jobID)

    // Decrement the queued job counter.
    QueueMetrics.Queued.labels(name).dec()
    // Record the time this job spent in the queue.
    QueueMetrics.Time.labels(name).observe(job.dispatched.until(OffsetDateTime.now(), ChronoUnit.MILLIS).toDouble() / 1000.0)

    // Attempt to execute the job.
    try {
      JobManager.setJobInProgress(job.jobID)

      when (JobExecutors.new(JobExecContext(name, job.jobID, job.body)).execute(job.jobID, job.body)) {
        PlatformJobResultStatus.Success -> handler.sendSuccess(SuccessNotification(job.jobID))
        PlatformJobResultStatus.Failure -> handler.sendError(ErrorNotification(job.jobID, 1))
        PlatformJobResultStatus.Aborted -> { /* Job was aborted, send no notifications. */ }
      }
    } catch (e: InterruptedException) {
      Log.error("job execution for ${job.jobID} was interrupted; likely timed out", e)
      handler.sendError(ErrorNotification(jobID = job.jobID, code = 1, message = e.message ?: "job timed out"))
    } catch (e: Throwable) {
      Log.error("job execution failed with an exception for job ${job.jobID}", e)
      handler.sendError(ErrorNotification(jobID = job.jobID, code = 1, message = e.message))
    }
  }
}
