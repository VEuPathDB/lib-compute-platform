package org.veupathdb.lib.compute.platform.intern.db.queries.update

import java.sql.Connection

private const val SQL = """
  UPDATE
    compute.jobs
  SET
    status = 'queued'
  , grabbed = null
  , finished = null
  WHERE
    status = 'in-progress'
"""

/**
 * Marks all `in-progress` jobs as `queued` and removes their grabbed timestamp.
 *
 * @param con Open database connection on which the query will be executed.
 */
internal fun QueueDeadJobs(con: Connection) {
  con.createStatement().use { stmt -> stmt.execute(SQL) }
}