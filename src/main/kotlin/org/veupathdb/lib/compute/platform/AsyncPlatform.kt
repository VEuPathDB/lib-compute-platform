package org.veupathdb.lib.compute.platform

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.intern.db.DatabaseMigrator
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecutors
import org.veupathdb.lib.compute.platform.intern.queues.JobQueues
import org.veupathdb.lib.compute.platform.intern.s3.S3
import org.veupathdb.lib.hash_id.HashID

object AsyncPlatform {

  private val Log = LoggerFactory.getLogger(javaClass)

  private var initialized = false

  /**
   * Initializes the async compute platform library.
   *
   * Initialization steps:
   * * configuration
   * * database migration
   * * dead job cleanup
   * * queue repopulation
   *
   * @param config Platform configuration.
   *
   * @throws IllegalStateException If this method is called more than once.
   */
  @JvmStatic
  fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize AsyncPlatform more than once!")

    initialized = true

    Log.info("Initializing async platform")

    // Initialize components.
    JobQueues.init(config)
    JobExecutors.init(config)
    QueueDB.init(config.dbConfig)
    S3.init(config.s3Config)

    // Perform database setup/migrations
    Log.info("Performing database migrations")
    DatabaseMigrator().run()

    // Cleanup dead jobs
    Log.info("Cleaning up dead jobs")
    QueueDB.deadJobCleanup()

    // Requeue everything
    Log.info("Resubmitting queued jobs")
    QueueDB.getQueuedJobs().use { stream ->
      stream.forEach {
        S3.resetWorkspace(it.jobID)
        JobQueues.submitJob(it.queue, it.jobID, it.config)
      }
    }
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
    Log.info("Submitting job {} to the async platform.", jobID)

    // If the target queue doesn't exist, halt here.
    if (queue !in JobQueues)
      throw IllegalStateException("Attempted to submit a job to nonexistent queue '$queue'.")

    // Record the new job in the database
    QueueDB.submitJob(queue, jobID, rawConfig?.toString())

    // Create a workspace for the new job in S3
    S3.submitWorkspace(jobID, rawConfig)

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
  fun getJob(jobID: HashID): AsyncJob? {
    Log.debug("Looking up job {} from either the managed DB or S3", jobID)

    val out = QueueDB.getJob(jobID) ?: S3.getJob(jobID)

    if (out == null)
      Log.debug("Job not found in either location.")

    return out
  }

  /**
   * Fetches the results files for the target job.
   *
   * This method makes no attempt to verify that the target job is actually
   * complete.  That check should be performed before calling this method.
   *
   * @param jobID Hash ID of the job whose results should be retrieved.
   *
   * @return List of result files in the job workspace.
   *
   * These files will be any non-flag, non-input-config file that exist in the
   * workspace.
   */
  @JvmStatic
  fun getJobResults(jobID: HashID): List<JobResultReference> {
    Log.debug("Fetching results for job {}", jobID)
    return S3.getResultFiles(jobID)
  }
}