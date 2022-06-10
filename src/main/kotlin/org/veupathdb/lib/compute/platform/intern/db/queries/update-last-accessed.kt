package org.veupathdb.lib.compute.platform.intern.db.queries

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  UPDATE
    compute.jobs
  SET
    last_accessed = now()
  WHERE
    job_id = ?
"""

/**
 * Updates the last_accessed field on the target job row with the current
 * timestamp.
 *
 * @param con Open database connection the query will be executed on.
 *
 * @param jobID Hash ID of the job whose record should be updated.
 */
internal fun UpdateDBLastAccessed(con: Connection, jobID: HashID) {
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.execute()
  }
}