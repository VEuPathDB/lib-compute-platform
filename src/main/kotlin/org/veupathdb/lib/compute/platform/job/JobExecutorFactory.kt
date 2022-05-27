package org.veupathdb.lib.compute.platform.job

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
   * Constructs a new [JobExecutor] instance with the given job ID and raw
   * configuration.
   *
   * @param jobID Hash ID of the job to execute.
   *
   * @param rawConfig Raw, nullable configuration for the job.
   *
   * @return A new [JobExecutor] instance.
   */
  fun newJobExecutor(jobID: HashID, rawConfig: JsonNode?): JobExecutor
}