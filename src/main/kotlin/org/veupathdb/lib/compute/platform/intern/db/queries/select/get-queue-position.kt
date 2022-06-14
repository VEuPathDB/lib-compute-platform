package org.veupathdb.lib.compute.platform.intern.db.queries.select

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
    AND created < coalesce((SELECT created FROM cutoff), now())
"""

/**
 * Fetches the current queue position for the target job.
 *
 * @param con Open database connection to use for the query.
 *
 * @param jobID Hash ID of the job whose queue position should be fetched.
 *
 * @return The current queue position of the target job.
 */
internal fun GetJobQueuePosition(con: Connection, jobID: HashID) =
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)

    ps.executeQuery().use { rs ->
      rs.next()
      rs.getInt(1)
    }
  }