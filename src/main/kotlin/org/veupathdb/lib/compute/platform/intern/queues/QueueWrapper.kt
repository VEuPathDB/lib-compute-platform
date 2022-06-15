package org.veupathdb.lib.compute.platform.intern.queues

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncQueueConfig
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecContext
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecutors
import org.veupathdb.lib.compute.platform.intern.metrics.JobMetrics
import org.veupathdb.lib.compute.platform.intern.metrics.QueueMetrics
import org.veupathdb.lib.compute.platform.job.JobResultStatus
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.rabbit.jobs.QueueConfig
import org.veupathdb.lib.rabbit.jobs.QueueDispatcher
import org.veupathdb.lib.rabbit.jobs.QueueWorker
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

  private val dispatch: QueueDispatcher
  private val handler: QueueWorker

  init {
    Log.info("initializing queue wrapper for queue {}", name)

    val qc = QueueConfig().also {
      it.hostname = conf.host
      it.hostPort = conf.port
      it.username = conf.username
      it.password = conf.password
      it.workers  = conf.workers

      it.jobQueueName = "${conf.id}_jobs"
      it.successQueueName = "${conf.id}_success"
      it.errorQueueName = "${conf.id}_error"
    }

    // Setup dispatch end of queue the wrapper
    dispatch = QueueDispatcher(qc)
    dispatch.onError(this::onError)
    dispatch.onSuccess(this::onSuccess)

    // Setup worker end of the queue wrapper
    handler  = QueueWorker(qc)
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
    QueueDB.markJobAsFailed(job.jobID)
  }

  private fun onSuccess(job: SuccessNotification) {
    Log.info("job {} succeeded", job.jobID)
    JobMetrics.Successes.labels(name).inc()
    QueueDB.markJobAsComplete(job.jobID)
  }

  private fun onJob(job: JobDispatch) {
    Log.debug("handling job {}", job.jobID)

    // Decrement the queued job counter.
    QueueMetrics.Queued.labels(name).dec()
    // Record the time this job spent in the queue.
    QueueMetrics.Time.labels(name).observe(job.dispatched.until(OffsetDateTime.now(), ChronoUnit.MILLIS).toDouble() / 1000.0)
    // Mark the job as in-progress in the database.
    QueueDB.markJobAsInProgress(job.jobID)

    // Attempt to execute the job.
    try {
      when (JobExecutors.new(JobExecContext(name, job.jobID, job.body)).execute(job.jobID, job.body)) {
        JobResultStatus.Success -> handler.sendSuccess(SuccessNotification(job.jobID))
        JobResultStatus.Failure -> handler.sendError(ErrorNotification(job.jobID, 1))
      }
    } catch (e: Throwable) {
      Log.error("job execution failed with an exception for job ${job.jobID}", e)
      handler.sendError(ErrorNotification(job.jobID, 1, e.message))
    }
  }
}