package org.veupathdb.lib.compute.platform.intern.db.queries.delete

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  DELETE FROM
    compute.jobs
  WHERE
    job_id = ?
"""

/**
 * Deletes a job from the database.
 *
 * @param con Open database connection to be used for executing the query.
 *
 * @param jobID Hash ID of the job to delete.
 *
 * @since 1.2.0
 */
internal fun DeleteJob(con: Connection, jobID: HashID) {
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.execute()
  }
}