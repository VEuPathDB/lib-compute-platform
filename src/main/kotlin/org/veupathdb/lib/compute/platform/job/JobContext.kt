package org.veupathdb.lib.compute.platform.job

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.hash_id.HashID

/**
 * Job Context
 *
 * Context in/for which a job will be executed.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
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