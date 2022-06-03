package org.veupathdb.lib.compute.platform.intern.db.queries

import org.veupathdb.lib.compute.platform.JobStatus
import org.veupathdb.lib.compute.platform.intern.db.model.JobRecord
import org.veupathdb.lib.compute.platform.intern.db.util.stream
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.jackson.Json
import java.sql.Connection
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.stream.Stream

private const val SQL = """
  SELECT
    job_id
  , status
  , queue
  , config
  , created
  , last_accessed
  , grabbed
  , finished
  FROM
    compute.jobs
  WHERE
    status = 'queued'
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
internal fun ListQueuedJobs(con: Connection): Stream<JobRecord> {
  // Nothing is closed in this method as the caller is responsible for closing
  // the returned stream (which will close the connection, statement, and
  // result-set)
  return con.createStatement().executeQuery(SQL).stream().map(::parseJobRecord)
}

private fun parseJobRecord(rs: ResultSet) =
  JobRecord(
    HashID(rs.getBytes(1)),                         // job_id
    JobStatus.fromString(rs.getString(2)),          // status
    rs.getString(3),                                // queue
    rs.getString(4)?.let(Json::parse),              // config
    rs.getObject(5, OffsetDateTime::class.java),    // created
    rs.getObject(6, OffsetDateTime::class.java),    // last_accessed
    rs.getObject(7, OffsetDateTime::class.java),    // grabbed
    rs.getObject(8, OffsetDateTime::class.java),    // finished
  )
