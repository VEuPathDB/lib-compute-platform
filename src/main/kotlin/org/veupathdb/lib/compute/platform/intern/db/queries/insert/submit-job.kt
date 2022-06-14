package org.veupathdb.lib.compute.platform.intern.db.queries.insert

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  INSERT INTO
    compute.jobs (
      job_id
    , status
    , queue
    , config
    , input_files
    , created
    , last_accessed
    )
  VALUES
    (
      ?        -- 1 - job_id
    , 'queued' --   - status
    , ?        -- 2 - queue
    , ?        -- 3 - config
    , ?        -- 4 - input_files
    , now()    --   - created
    , now()    --   - last_accessed
    )
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
internal fun RecordNewJob(con: Connection, jobID: HashID, queue: String, config: String?, inputFiles: Iterable<String>) {
  con.prepareStatement(SQL).use { ps ->
    ps.setBytes(1, jobID.bytes)
    ps.setString(2, queue)
    ps.setString(3, config)
    ps.setArray(4, con.createArrayOf("VARCHAR", inputFiles.toList().toTypedArray()))
    ps.execute()
  }

}