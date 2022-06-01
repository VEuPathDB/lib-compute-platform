package org.veupathdb.lib.compute.platform.intern.db.queries

import org.veupathdb.lib.compute.platform.intern.db.model.ParseJobRecord
import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  SELECT
    job_id
  , status
  , queue
  , config
  , created
  , grabbed
  , finished
  FROM
    compute.jobs
  WHERE
    job_id = ?
"""

internal fun LookupJob(con: Connection, jobID: HashID) =
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.executeQuery().use {
      if (it.next())
        ParseJobRecord(it)
      else
        null
    }
  }