package org.veupathdb.lib.compute.platform

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.intern.db.DatabaseMigrator
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecutors
import org.veupathdb.lib.compute.platform.intern.queues.JobQueues
import org.veupathdb.lib.compute.platform.intern.s3.QueueS3
import org.veupathdb.lib.hash_id.HashID

object AsyncPlatform {

  private var initialized = false

  @JvmStatic
  fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize AsyncPlatform more than once!")

    initialized = true

    // Initialize components.
    JobQueues.init(config)
    JobExecutors.init(config)
    QueueDB.init(config.dbConfig)
    QueueS3.init(config.s3Config)

    // Perform database setup/migrations
    DatabaseMigrator().run()

    // Cleanup dead jobs
    TODO("dead job cleanup")

    // Resubmit jobs to the queues
    QueueDB.getQueuedJobs().use(JobQueues::resubmitAll)
  }

  /**
   * Submits a new job to be processed asynchronously.
   *
   * @param queue Name/ID of the queue that this job should be submitted to.
   *
   * @param
   */
  @JvmStatic
  @JvmOverloads
  fun submitJob(queue: String, jobID: HashID, rawConfig: JsonNode? = null) {
    // If the target queue doesn't exist, halt here.
    if (queue !in JobQueues)
      throw IllegalStateException("Attempted to submit a job to nonexistent queue '$queue'.")

    // Record the new job in the database
    QueueDB.submitJob(queue, jobID, rawConfig?.toString())

    // Create a workspace for the new job in S3
    QueueS3.submitJob(jobID, rawConfig)

    // Submit the new job to the target job queue
    JobQueues.submitJob(queue, jobID, rawConfig)
  }

  /**
   * Looks up and returns the job with the given [jobID] if such a job exists.
   *
   * If no such job was found, this method returns `null`.
   *
   * @param jobID Hash ID of the job to look up.
   *
   * @return The target job, if it exists, otherwise `null`.
   */
  @JvmStatic
  fun getJob(jobID: HashID) = QueueDB.getJob(jobID) ?: QueueS3.getJob(jobID)

  @JvmStatic
  fun getJobResults(jobID: HashID): List<ResultReference>
}