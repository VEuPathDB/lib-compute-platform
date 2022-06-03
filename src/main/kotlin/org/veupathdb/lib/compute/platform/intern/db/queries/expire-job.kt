package org.veupathdb.lib.compute.platform.intern.db.queries

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  UPDATE
    compute.jobs
  SET
    status = 'expired'
  WHERE
    job_id = ?
"""

/**
 * Marks the target job as expired in the database.
 *
 * @param con Open database connection t obe used for the query.
 *
 * @param jobID Hash ID of the job to mark as expired.
 */
internal fun MarkJobExpired(con: Connection, jobID: HashID) {
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.execute()
  }
}