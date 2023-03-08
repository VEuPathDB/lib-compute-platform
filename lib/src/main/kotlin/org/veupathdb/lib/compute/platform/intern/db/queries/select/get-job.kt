package org.veupathdb.lib.compute.platform.intern.db.queries.select

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

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
        it.toJobRow()
      else
        null
    }
  }
