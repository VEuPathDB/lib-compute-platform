package org.veupathdb.lib.compute.platform.db.queries

import org.veupathdb.lib.compute.platform.JobStatus
import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection

object SelectJobStatus {
  const val SQL = """
    SELECT
      status
    FROM
      compute.jobs
    WHERE
      job_id = ?
  """

  @JvmStatic
  fun execute(con: Connection, jobID: HashID): JobStatus? {

  }
}