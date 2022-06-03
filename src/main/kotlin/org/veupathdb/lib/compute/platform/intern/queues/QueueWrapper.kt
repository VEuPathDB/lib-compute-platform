package org.veupathdb.lib.compute.platform.intern.queues

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncQueueConfig
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecutors
import org.veupathdb.lib.compute.platform.JobResultStatus
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.mtx.JobMetrics
import org.veupathdb.lib.compute.platform.intern.mtx.QueueMetrics
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.rabbit.jobs.QueueConfig
import org.veupathdb.lib.rabbit.jobs.QueueDispatcher
import org.veupathdb.lib.rabbit.jobs.QueueWorker
import org.veupathdb.lib.rabbit.jobs.model.ErrorNotification
import org.veupathdb.lib.rabbit.jobs.model.JobDispatch
import org.veupathdb.lib.rabbit.jobs.model.SuccessNotification
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

internal class QueueWrapper(conf: AsyncQueueConfig) {

  private val Log = LoggerFactory.getLogger(this::class.java)

  val name = conf.id

  private val dispatch: QueueDispatcher
  private val handler: QueueWorker

  init {
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

    dispatch = QueueDispatcher(qc)
    handler  = QueueWorker(qc)

    handler.onJob(this::onJob)
  }

  fun submitJob(jobID: HashID, config: JsonNode? = null) {
    QueueMetrics.Queued.inc()
    dispatch.dispatch(JobDispatch(jobID, config))
  }

  private fun onJob(job: JobDispatch) {
    // Decrement the queued job counter.
    QueueMetrics.Queued.dec()
    // Record the time this job spent in the queue.
    QueueMetrics.Time.observe(job.dispatched.until(OffsetDateTime.now(), ChronoUnit.MILLIS).toDouble() / 1000.0)

    // Mark the job as grabbed in the database.
    QueueDB.markJobAsGrabbed(job.jobID)

    try {
      when (JobExecutors.new(job.jobID, job.body).execute()) {
        JobResultStatus.Success -> sendSuccess(job.jobID)
        JobResultStatus.Failure -> sendError(job.jobID)
      }
    } catch (e: Throwable) {
      Log.error("Job execution failed!", e)
      sendError(job.jobID)
    } finally {
      // TODO: Mark job as finished in the DB
    }
  }

  private fun sendError(jobID: HashID, msg: String? = null) {
    JobMetrics.Failures.inc()
    if (msg == null)
      handler.sendError(ErrorNotification(jobID, 1))
    else
      handler.sendError(ErrorNotification(jobID, 1, msg))
  }

  private fun sendSuccess(jobID: HashID) {
    JobMetrics.Successes.inc()
    handler.sendSuccess(SuccessNotification(jobID))
  }
}