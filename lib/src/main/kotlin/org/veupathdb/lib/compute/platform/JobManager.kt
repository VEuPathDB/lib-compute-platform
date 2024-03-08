package org.veupathdb.lib.compute.platform

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.intern.db.AsyncDBJob
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.s3.AsyncS3Job
import org.veupathdb.lib.compute.platform.intern.s3.S3
import org.veupathdb.lib.compute.platform.job.AsyncJob
import org.veupathdb.lib.hash_id.HashID
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal object JobManager {
  private val Log = LoggerFactory.getLogger(javaClass)
  private val LocksByJobID: Cache<String, ReentrantLock> = Caffeine.newBuilder()
    .expireAfterWrite(30L, TimeUnit.MINUTES) // Expire after 30 minutes to free up memory.
    .build()

  fun getJob(jobID: HashID): Pair<AsyncDBJob?, AsyncS3Job?> {
    val lock = ensureLock(jobID)
    return lock.withLock { QueueDB.getJob(jobID) to S3.getJob(jobID) }
  }

  fun getAndReconcileJob(jobID: HashID): AsyncJob? {
    val (dbJob, s3Job) = getJob(jobID)
    val lock = ensureLock(jobID)
    if (dbJob != null) {
      Log.debug("job {} found in the managed database", jobID)

      // If the status as determined by looking at S3 does not align with the
      // status we last knew in our internal database, then another campus has
      // claimed ownership of the job.
      if (lock.withLock { s3Job != null && s3Job.status != dbJob.status }) { // Lock while checking lazy initialized statuses to ensure nothing changes from under us.
        Log.debug("deleting job {} from queue db as it has a different status in S3 than the queue DB: s3.status = {}, db.status = {}", jobID, s3Job?.status, dbJob.status)

        // Delete our DB record for the job and return the S3 instance instead.
        QueueDB.deleteJob(jobID)
        return s3Job
      }

      // If job is not in S3, but is in the database, something has gone wrong. Remove the job from the internal DB.
      // Otherwise, the job will be stuck indefinitely.
      if (lock.withLock { s3Job == null }) {
        Log.debug("Job {} is missing in S3 but is present in queue db. Deleting job from queue db to start fresh.")

        QueueDB.deleteJob(jobID)
        return null
      }

      // The statuses did align, so we (this service instance) presumably still
      // own the job.

      Log.debug("updating last accessed date for job {}", jobID)

      // update it's last accessed date
      QueueDB.updateJobLastAccessed(jobID)
      // and return it
      return dbJob
    }

    if (s3Job != null) {
      Log.debug("job {} found in S3", jobID)
      // return it
      return s3Job
    }

    return null;
  }

  fun setJobQueued(jobID: HashID, queue: String) {
    val lock = ensureLock(jobID)
    lock.withLock {
      QueueDB.markJobAsQueued(jobID, queue)
      S3.markWorkspaceAsQueued(jobID)
    }
  }

  fun setJobInProgress(jobID: HashID) {
    val lock = ensureLock(jobID)
    lock.withLock {
      QueueDB.markJobAsInProgress(jobID)
      S3.markWorkspaceAsInProgress(jobID)
    }
  }

  fun setJobComplete(jobID: HashID) {
    val lock = ensureLock(jobID)
    lock.withLock {
      QueueDB.markJobAsComplete(jobID)
      S3.markWorkspaceAsComplete(jobID)
    }
  }

  fun setJobFailed(jobID: HashID) {
    val lock = ensureLock(jobID)
    lock.withLock {
      QueueDB.markJobAsFailed(jobID)
      S3.markWorkspaceAsFailed(jobID)
    }
  }

  fun setJobExpired(jobID: HashID) {
    val lock = ensureLock(jobID)
    lock.withLock {
      QueueDB.markJobAsExpired(jobID)
      S3.expireWorkspace(jobID)
    }
  }

  private fun ensureLock(jobID: HashID): ReentrantLock {
    return LocksByJobID.get(jobID.string) { ReentrantLock() }
  }
}