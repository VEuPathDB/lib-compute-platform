package org.veupathdb.lib.compute.platform.intern.db.queries

import org.veupathdb.lib.compute.platform.JobStatus
import org.veupathdb.lib.compute.platform.intern.db.model.JobRecord
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.jackson.Json
import java.sql.Connection
import java.sql.ResultSet
import java.time.OffsetDateTime

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
    job_id = ?
"""

/**
 * Looks up the target job in the database.
 *
 * @param con Open database connection to be used for the query.
 *
 * @param jobID Hash ID of the job to get.
 *
 * @return The target job record, if such a record exists, otherwise `null`.
 */
internal fun LookupJob(con: Connection, jobID: HashID) =
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.executeQuery().use {
      if (it.next())
        parseJobRecord(it)
      else
        null
    }
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
