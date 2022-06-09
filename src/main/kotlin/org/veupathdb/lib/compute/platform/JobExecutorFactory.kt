package org.veupathdb.lib.compute.platform

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.hash_id.HashID

/**
 * Job Executor Provider/Factory
 *
 * Provides [JobExecutor] instances to be used when running jobs from the
 * internal job queue(s).
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
interface JobExecutorFactory {

  /**
   * Constructs a new [JobExecutor] instance.
   *
   * @param jobID Hash ID of the job that is to be executed.
   *
   * @param config Raw/serialized configuration for the job to be executed.
   *
   * @return A new [JobExecutor] instance.
   */
  fun newJobExecutor(jobID: HashID, config: JsonNode?): JobExecutor
}