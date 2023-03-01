package org.veupathdb.lib.compute.platform

import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.intern.JobPruner
import org.veupathdb.lib.compute.platform.intern.db.DatabaseMigrator
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecutors
import org.veupathdb.lib.compute.platform.intern.queues.JobQueues
import org.veupathdb.lib.compute.platform.intern.s3.S3
import org.veupathdb.lib.compute.platform.intern.ws.ScratchSpaces
import org.veupathdb.lib.compute.platform.job.AsyncJob
import org.veupathdb.lib.compute.platform.job.JobFileReference
import org.veupathdb.lib.compute.platform.job.JobSubmission
import org.veupathdb.lib.hash_id.HashID

/**
 * Asynchronous Compute Platform
 *
 * Access point for working with the async compute platform library.
 *
 * Provides methods for submitting jobs, retrieving job details, and fetching
 * job results.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
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
    QueueDB.init(config)
    S3.init(config.s3Config)
    ScratchSpaces.init(config)

    // Perform database setup/migrations
    // See: src/main/resources/db/migrations/readme.adoc
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

    // Schedule the expired job pruner
    JobPruner.schedule()
  }

  inline fun init(fn: AsyncPlatformConfig.Builder.() -> Unit) {
    init(AsyncPlatformConfig.build(fn))
  }

  /**
   * Submits a new job to the async compute platform.
   *
   * @param queue ID/name of the target queue this job should be submitted to.
   *
   * @param fn Action used to configure the job to submit.
   *
   * @throws IllegalArgumentException If the given [queue] value is not a valid
   * queue ID/name.
   */
  @JvmStatic
  inline fun submitJob(queue: String, fn: JobSubmission.Builder.() -> Unit) {
    submitJob(queue, JobSubmission.build(fn))
  }

  /**
   * Submits a new job to the async compute platform.
   *
   * @param queue ID/name of the target queue this job should be submitted to.
   *
   * @param job Configuration for the job to submit.
   *
   * @throws IllegalArgumentException If the given [queue] value is not a valid
   * queue ID/name.
   *
   * @throws IllegalStateException If the ID of the given job already exists and
   * belongs to another instance of this service.
   */
  @JvmStatic
  fun submitJob(queue: String, job: JobSubmission) {
    Log.info("submitting job {} to the async platform", job.jobID)

    // If the target queue doesn't exist, halt here.
    if (queue !in JobQueues)
      throw IllegalArgumentException("Attempted to submit a job to nonexistent queue '$queue'.")

    // Lookup the job to see if it already exists.
    val exists = getJob(job.jobID)
      // If it does exist
      ?.also {
        // And it is not owned by this service instance
        if (!it.owned)
          // Throw an exception
          throw IllegalStateException("Attempted to submit a job that would overwrite an existing job owned by another campus (${job.jobID})")
      }
      .let { it != null }

    // If the job already exists
    if (exists)
      // Reset the job status to queued and update the queue name
      QueueDB.markJobAsQueued(job.jobID, queue)
    // Else, if the job does not already exist
    else
      // Record the new job in the database
      QueueDB.submitJob(queue, job.jobID, job.config?.toString(), job.inputs.keys)

    // Remove any previous workspace at this location
    S3.deleteWorkspace(job.jobID, false)

    // Create a workspace for the new job in S3
    S3.submitWorkspace(job.jobID, job.config, job.inputs)

    // Submit the new job to the target job queue
    JobQueues.submitJob(queue, job.jobID, job.config)
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

    // Check to see if the job exists in the managed DB
    QueueDB.getJob(jobID)?.also {
      // It does...
      Log.debug("Job found in the managed database")
      // update it's last accessed date
      QueueDB.updateJobLastAccessed(jobID)
      // and return it
      return it
    }

    // Check to see if the job exists in S3
    S3.getJob(jobID)?.also {
      // it does
      Log.debug("Job found in S3")
      // return it
      return it
    }

    // Job wasn't found in either the managed DB or in S3
    return null
  }

  /**
   * Fetches the available files for the target job.
   *
   * @param jobID Hash ID of the job whose results should be retrieved.
   *
   * @return List of files in the job workspace.
   */
  @JvmStatic
  fun getJobFiles(jobID: HashID): List<JobFileReference> {
    Log.debug("Fetching results for job {}", jobID)
    return S3.getNonReservedFiles(jobID)
  }

  /**
   * Deletes the target job only if it exists, is owned by the current service
   * or process, and is in a completed status.
   *
   * If the target job does not exist, is not owned by the current service or
   * process, or is not in a completed status, this method throws an
   * [IllegalStateException].
   *
   * Callers should first use [getJob] to test the completion and ownership
   * statuses of the job before attempting to use this method.
   *
   * @param jobID Hash ID of the job that should be deleted.
   *
   * @throws IllegalStateException If the target job does not exist, is not
   * owned by the current service or process, or is not in a completed status.
   *
   * @since 1.2.0
   */
  @JvmStatic
  fun deleteJob(jobID: HashID) {
    Log.debug("Deleting job {}", jobID)

    // Assert that the job is both owned by this process and is complete
    (QueueDB.getJob(jobID)
      ?: throw IllegalStateException("Attempted to delete unowned job $jobID"))
      .finished ?: throw IllegalStateException("Attempted to delete incomplete job $jobID")

    QueueDB.deleteJob(jobID)
    S3.deleteWorkspace(jobID)
  }
}