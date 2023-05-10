package org.veupathdb.lib.compute.platform

import org.veupathdb.lib.compute.platform.intern.db.AsyncDBJob
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.s3.AsyncS3Job
import org.veupathdb.lib.compute.platform.intern.s3.S3
import org.veupathdb.lib.hash_id.HashID
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal object JobManager {
  private val lock = ReentrantLock()

  fun getJob(jobID: HashID): Pair<AsyncDBJob?, AsyncS3Job?> {
    return lock.withLock { QueueDB.getJob(jobID) to S3.getJob(jobID) }
  }

  fun setJobQueued(jobID: HashID, queue: String) {
    lock.withLock {
      QueueDB.markJobAsQueued(jobID, queue)
      S3.markWorkspaceAsQueued(jobID)
    }
  }

  fun setJobInProgress(jobID: HashID) {
    lock.withLock {
      QueueDB.markJobAsInProgress(jobID)
      S3.markWorkspaceAsInProgress(jobID)
    }
  }

  fun setJobComplete(jobID: HashID) {
    lock.withLock {
      QueueDB.markJobAsComplete(jobID)
      S3.markWorkspaceAsComplete(jobID)
    }
  }

  fun setJobFailed(jobID: HashID) {
    lock.withLock {
      QueueDB.markJobAsFailed(jobID)
      S3.markWorkspaceAsFailed(jobID)
    }
  }

  fun setJobExpired(jobID: HashID) {
    lock.withLock {
      QueueDB.markJobAsExpired(jobID)
      S3.expireWorkspace(jobID)
    }
  }
}