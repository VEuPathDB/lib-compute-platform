package org.veupathdb.lib.compute.platform.intern.db.queries.update

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  UPDATE
    compute.jobs
  SET
    queue = ?
  , status = 'queued'
  WHERE
    job_id = ?
"""

/**
 * Resets the target job to queued in the database.
 *
 * @param con Open database connection to be used for the query.
 *
 * @param jobID Hash ID of the job to mark as expired.
 *
 * @param queue Name of the new queue the job was submitted to.
 */
internal fun MarkJobQueued(con: Connection, jobID: HashID, queue: String) {
  con.prepareStatement(SQL).use { ps ->
    ps.setString(1, queue)
    ps.setBytes(2, jobID.bytes)
    ps.execute()
  }
}