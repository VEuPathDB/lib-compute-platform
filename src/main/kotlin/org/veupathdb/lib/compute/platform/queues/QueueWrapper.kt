package org.veupathdb.lib.compute.platform.queues

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.conf.AsyncQueueConfig
import org.veupathdb.lib.compute.platform.job.JobExecutors
import org.veupathdb.lib.compute.platform.job.JobResultStatus
import org.veupathdb.lib.compute.platform.mtx.JobMetrics
import org.veupathdb.lib.compute.platform.mtx.QueueMetrics
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.rabbit.jobs.QueueConfig
import org.veupathdb.lib.rabbit.jobs.QueueDispatcher
import org.veupathdb.lib.rabbit.jobs.QueueWorker
import org.veupathdb.lib.rabbit.jobs.model.ErrorNotification
import org.veupathdb.lib.rabbit.jobs.model.JobDispatch
import org.veupathdb.lib.rabbit.jobs.model.SuccessNotification

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
    QueueMetrics.Queued.dec()

    // TODO: get the queue time and record it
    // TODO: mark job as grabbed in the DB

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