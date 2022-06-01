package org.veupathdb.lib.compute.platform.intern.db.queries

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  WITH cutoff AS (
    SELECT
      created
    FROM
      compute.jobs
    WHERE
      job_id = ?
  )
  SELECT
    count(1)
  FROM
    compute.jobs
  WHERE
    status = 'queued'
    AND cutoff < (SELECT created FROM cutoff)
"""

/**
 * Fetches the current queue position for the target job.
 */
internal fun GetJobQueuePosition(con: Connection, jobID: HashID) =
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)

    ps.executeQuery().use { rs ->
      rs.next()
      rs.getInt(1)
    }
  }