package org.veupathdb.lib.compute.platform.intern.db.queries.update

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

private const val SQL = """
  UPDATE
    compute.jobs
  SET
    output_files = ?
  WHERE
    job_id = ?
"""

internal fun SetJobOutputFiles(con: Connection, jobID: HashID, files: Array<String>) {
  con.prepareStatement(SQL).use {
    it.setArray(1, con.createArrayOf("VARCHAR", files))
    it.setBytes(2, jobID.bytes)
    it.execute()
  }
}

