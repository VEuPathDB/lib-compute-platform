package org.veupathdb.lib.compute.platform.intern.db.queries

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  INSERT INTO
    compute.jobs (
      job_id
    , status
    , queue
    , config
    , created
    )
  VALUES
    (?, 'queued', ?, ?, now())
"""

internal fun RecordNewJob(con: Connection, jobID: HashID, queue: String, config: String?) {
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.setString(2, queue)
    ps.setString(3, config)
    ps.execute()
  }
}