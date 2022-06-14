package org.veupathdb.lib.compute.platform.job

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.hash_id.HashID

interface JobContext {

  /**
   * ID the job represented by this context.
   */
  val jobID: HashID

  /**
   * Configuration submitted for this job.
   */
  val config: JsonNode?

  /**
   * Local scratch workspace to be used by this job.
   */
  val workspace: JobWorkspace
}