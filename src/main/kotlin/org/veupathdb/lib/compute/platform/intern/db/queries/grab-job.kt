package org.veupathdb.lib.compute.platform.intern.db.queries

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

internal fun GrabJob(con: Connection, jobID: HashID) {
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.execute()
  }
}