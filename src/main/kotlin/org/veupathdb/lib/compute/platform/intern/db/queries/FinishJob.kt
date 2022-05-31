package org.veupathdb.lib.compute.platform.intern.db.queries

import org.veupathdb.lib.compute.platform.JobStatus
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

internal fun FinishJob(con: Connection, jobID: HashID, status: JobStatus) {
  if (status != JobStatus.Complete && status != JobStatus.Failed)
    throw IllegalStateException("Cannot mark a job as finished with a non-finished status!")

  con.prepareStatement(SQL).use { ps ->

  }

}