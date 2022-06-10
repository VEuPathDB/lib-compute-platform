package org.veupathdb.lib.compute.platform.intern.jobs

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.JobExecutor
import org.veupathdb.lib.compute.platform.JobResult
import org.veupathdb.lib.compute.platform.JobResultStatus
import org.veupathdb.lib.compute.platform.intern.s3.S3
import org.veupathdb.lib.compute.platform.intern.ws.ScratchSpaces
import org.veupathdb.lib.hash_id.HashID

internal class JobExecutionHandler(private val executor: JobExecutor) {
  fun execute(jobID: HashID, conf: JsonNode?): JobResultStatus {
    val workspace = ScratchSpaces.create(jobID)

    try {
      val res = executor.execute(JobCTX(jobID, conf, workspace))
      S3.persistFiles(jobID, workspace.getFiles(res.outputFiles))
      return res.status
    } finally {
      workspace.delete()
    }
  }
}