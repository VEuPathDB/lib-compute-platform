package org.veupathdb.lib.compute.platform.intern.jobs

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.JobExecutor
import org.veupathdb.lib.compute.platform.JobResultStatus
import org.veupathdb.lib.compute.platform.intern.s3.S3
import org.veupathdb.lib.compute.platform.intern.ws.ScratchSpaces
import org.veupathdb.lib.hash_id.HashID

class JobExecutionHandler(
  private val executor: JobExecutor,
  private val persistable: List<String>,
) {
  fun execute(jobID: HashID, conf: JsonNode?): JobResultStatus {
    val workspace = ScratchSpaces.create(jobID)

    try {
      return executor.execute(JobCTX(jobID, conf, workspace))
    } finally {
      S3.persistFiles(jobID, workspace.getFiles(persistable))
      workspace.delete()
    }
  }
}