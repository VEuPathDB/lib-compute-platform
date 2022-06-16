package org.veupathdb.lib.compute.platform.job

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.hash_id.HashID

/**
 * Job Executor Context
 *
 * Context for which a new [JobExecutor] instance is being requested.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
interface JobExecutorContext {

  /**
   * ID/Name of the queue for which the [JobExecutor] has been requested.
   */
  val queue: String

  /**
   * Hash ID of the job for which the [JobExecutor] has been requested.
   */
  val jobID: HashID

  /**
   * Optional configuration of the job for which the [JobExecutor] has been
   * requested.
   */
  val jobConfig: JsonNode?
}