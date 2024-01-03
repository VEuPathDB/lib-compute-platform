package org.veupathdb.lib.compute.platform.intern.db.queries.select

import org.veupathdb.lib.compute.platform.job.InternalJobRecord
import org.veupathdb.lib.compute.platform.intern.db.util.stream
import java.sql.Connection
import java.sql.ResultSet
import java.util.stream.Stream

// language=postgresql
private const val SQL = """
  SELECT
    job_id
  , status
  , queue
  , config
  , input_files
  , created
  , last_accessed
  , grabbed
  , finished
  FROM
    compute.jobs
  ORDER BY
    created ASC
"""

/**
 * Fetches a stream over all the jobs in the database currently in the `queued`
 * status, ordered by job creation date ascending.
 *
 * The returned stream wraps the live database [ResultSet] and **MUST** be
 * closed when the caller is done with it.
 *
 * @param con Open database connection that will be used to execute the query.
 *
 * @return A stream over the `queued` jobs in the database.
 *
 * **WARNING**: The returned stream **MUST** be closed on completion to avoid DB
 * connection leaks.
 */
internal fun ListAllJobs(con: Connection): Stream<InternalJobRecord> {
  // Nothing is closed in this method as the caller is responsible for closing
  // the returned stream (which will close the connection, statement, and
  // result-set)
  return con.createStatement().executeQuery(SQL).stream().map(ResultSet::toJobRow)
}
