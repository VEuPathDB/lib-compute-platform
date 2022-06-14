package org.veupathdb.lib.compute.platform.intern.db.queries.update

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  UPDATE
    compute.jobs
  SET
    status = 'grabbed'
  , grabbed = now()
  WHERE
    job_id = ?
"""

/**
 * Marks the target job as grabbed.
 *
 * @param con Open database connection to use for the query.
 *
 * @param jobID Hash ID of the target job that will be marked as a grabbed.
 */
internal fun MarkJobGrabbed(con: Connection, jobID: HashID) {
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.execute()
  }
}