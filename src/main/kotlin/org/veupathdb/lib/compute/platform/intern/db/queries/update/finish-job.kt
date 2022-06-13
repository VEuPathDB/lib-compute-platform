package org.veupathdb.lib.compute.platform.intern.db.queries.update

import org.veupathdb.lib.compute.platform.job.JobStatus
import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  UPDATE
    compute.jobs
  SET
    status = ?
  , finished = now()
  WHERE
    job_id = ?
"""

/**
 * Marks the target job as finished with the given completion status.
 *
 * @param con Open database connection to be used for the query.
 *
 * @param jobID Hash ID of the job to mark as completed
 *
 * @param status Completion status.  Must be one of `complete` or `failed`.
 *
 * @throws IllegalArgumentException If the given status is not one of
 * [JobStatus.Complete] or [JobStatus.Failed].
 */
internal fun MarkJobFinished(con: Connection, jobID: HashID, status: JobStatus) {
  if (status != JobStatus.Complete && status != JobStatus.Failed)
    throw IllegalArgumentException("Cannot mark a job as finished with a non-finished status!")

  con.prepareStatement(SQL).use { ps ->
    ps.setString(1, status.toString())
    ps.setBytes(2, jobID.bytes)
    ps.execute()
  }
}