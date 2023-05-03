package org.veupathdb.lib.compute.platform

import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.errors.UnownedJobException
import org.veupathdb.lib.compute.platform.intern.JobPruner
import org.veupathdb.lib.compute.platform.intern.db.DatabaseMigrator
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecutors
import org.veupathdb.lib.compute.platform.intern.queues.JobQueues
import org.veupathdb.lib.compute.platform.intern.s3.S3
import org.veupathdb.lib.compute.platform.intern.ws.ScratchSpaces
import org.veupathdb.lib.compute.platform.job.AsyncJob
import org.veupathdb.lib.compute.platform.job.JobFileReference
import org.veupathdb.lib.compute.platform.job.JobStatus
import org.veupathdb.lib.compute.platform.job.JobSubmission
import org.veupathdb.lib.compute.platform.model.JobReference
import org.veupathdb.lib.hash_id.HashID
import java.util.stream.Stream

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

    // Requeue everything that was queued or running.
    Log.info("Resubmitting queued jobs")
    QueueDB.getRunningJobs().use { running ->
      QueueDB.getQueuedJobs().use { queued ->
        Stream.concat(running, queued)
          .forEach {
            val s3Job = S3.getJob(it.jobID)

            // If the job doesn't exist in S3, then the cache was cleared or
            // something funky happened.  In this case, delete the job from
            // postgres.  Next time they attempt to run the job it will be
            // recreated.  This _does_ mean that in the event that the cache is
            // cleared, all the jobs that were queued at that time will be deleted.
            if (s3Job == null) {
              QueueDB.deleteJob(it.jobID)
            }

            // The job does exist in S3; resubmit it to the queue.
            else {
              JobQueues.submitJob(it.queue, it.jobID, it.config)
            }
          }
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
   * @throws IllegalStateException If the ID of the given job already exists,
   * belongs to another instance of this service, and is not expired.
   */
  @JvmStatic
  fun submitJob(queue: String, job: JobSubmission) {
    Log.info("submitting job {} to the async platform", job.jobID)

    // If the target queue doesn't exist, halt here.
    if (queue !in JobQueues)
      throw IllegalArgumentException("Attempted to submit a job to nonexistent queue '$queue'.")

    // Lookup the job to see if it already exists.
    // This call checks the managed DB and S3
    val existingJob = getJob(job.jobID)
      // If it does exist
      ?.also {
        // And it is not owned by this service instance AND the job is not
        // expired, bail here.
        if (!it.owned && it.status != JobStatus.Expired)
          // Throw an exception
          throw IllegalStateException("Attempted to submit a job that would overwrite an existing, non-expired job owned by another campus (${job.jobID})")
      }

    // If the job already exists
    if (existingJob != null && existingJob.owned) {
      Log.debug("job {} exists and is owned by this campus, marking the job as queued in the database",  job.jobID)
      // Reset the job status to queued and update the queue name
      QueueDB.markJobAsQueued(job.jobID, queue)
    }

    // Else, if the job does not already exist
    else {
      Log.debug("job {} does not exist, or is expired and is not owned by the current campus, submitting job to this campus' database", job.jobID)
      // Record the new job in the database
      QueueDB.submitJob(queue, job.jobID, job.config?.toString(), job.inputs.keys)
    }

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
      Log.debug("job {} found in the managed database", jobID)

      val s3Job = S3.getJob(jobID)

      // If the status as determined by looking at S3 does not align with the
      // status we last knew in our internal database, then another campus has
      // claimed ownership of the job.
      if (s3Job != null && s3Job.status != it.status) {
        Log.debug("deleting job {} from queue db as it has a different status in S3 than the queue DB: s3.status = {}, db.status = {}", jobID, s3Job.status, it.status)

        // Delete our DB record for the job and return the S3 instance instead.
        QueueDB.deleteJob(jobID)
        return s3Job
      }

      // The statuses did align, so we (this service instance) presumably still
      // own the job.

      Log.debug("updating last accessed date for job {}", jobID)

      // update it's last accessed date
      QueueDB.updateJobLastAccessed(jobID)
      // and return it
      return it
    }

    // Check to see if the job exists in S3
    S3.getJob(jobID)?.also {
      // it does
      Log.debug("job {} found in S3", jobID)
      // return it
      return it
    }

    Log.debug("job {} not found in the managed database or S3", jobID)

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
   * Fetches the target file for the target job.
   *
   * If the target file does not exist, this method returns `null`.
   *
   * @param jobID Hash ID of the job whose file should be retrieved.
   *
   * @param fileName Name of the file to retrieve.
   *
   * @return A reference to the target file, if it exists, otherwise `null`.
   *
   * @since 1.5.0
   */
  fun getJobFile(jobID: HashID, fileName: String): JobFileReference? {
    Log.debug("fetching target file \"{}\" for job {}", jobID, fileName)
    return S3.getJobFile(jobID, fileName)
  }

  /**
   * Deletes the target job only if it exists, is owned by the current service
   * or process, and is in a completed status.
   *
   * If the target job is not owned by the current service instance, this method
   * throws an [UnownedJobException].
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
    with(QueueDB.getJob(jobID) ?: throw UnownedJobException("Attempted to delete unowned job $jobID")) {
      if (finished == null)
        Log.info("deleting incomplete job {}", jobID)
    }

    QueueDB.deleteJob(jobID)
    S3.deleteWorkspace(jobID)
  }

  /**
   * Lists references to jobs that may or may not be owned by this service
   * instance.
   *
   * @return List of job references.
   *
   * @since 1.4.0
   */
  @JvmStatic
  fun listJobReferences(): List<JobReference> {
    val ownedJobs = HashMap<HashID, JobReference>()

    QueueDB.getAllJobs()
      .map { JobReference(it.jobID, true) }
      .forEach { ownedJobs[it.jobID] = it }

    S3.listJobIDs()
      .filter { it !in ownedJobs }
      .map { JobReference(it, false) }
      .forEach { ownedJobs[it.jobID] = it }

    return ownedJobs.values.toTypedArray().asList()
  }

  /**
   * Marks the target job as expired.
   *
   * If the target job is not owned by the current Async Platform instance, an
   * [UnownedJobException] will be thrown.
   *
   * @param jobID ID of the job to expire.
   *
   * @since 1.5.0
   */
  @JvmStatic
  fun expireJob(jobID: HashID) {
    Log.debug("Expiring job {}", jobID)

    // Verify that this job exists and is owned bt the current platform
    // instance.
    with(QueueDB.getJob(jobID) ?: throw UnownedJobException("Attempted to expire unowned job $jobID")) {
      if (finished == null)
        Log.info("expiring incomplete job {}", jobID)
    }

    QueueDB.markJobAsExpired(jobID)
    S3.expireWorkspace(jobID)
  }
}