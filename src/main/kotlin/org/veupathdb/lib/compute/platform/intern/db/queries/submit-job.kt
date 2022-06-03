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
    , last_accessed
    )
  VALUES
    (?, 'queued', ?, ?, now(), now())
"""

/**
 * Records a new job in the database.
 *
 * @param con Open database to be used for executing the query.
 *
 * @param jobID Hash ID of the job to record.
 *
 * @param queue Name/ID of the queue this job was/will be submitted to.
 *
 * @param config Optional raw configuration for the job to record.
 */
internal fun RecordNewJob(con: Connection, jobID: HashID, queue: String, config: String?) {
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.setString(2, queue)
    ps.setString(3, config)
    ps.execute()
  }
}