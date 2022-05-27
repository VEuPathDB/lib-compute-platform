package org.veupathdb.lib.compute.platform.jobs

/**
 * Async Job Executor
 *
 * Executes a configured job.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
interface JobExecutor {

  /**
   * Executes the configured job.
   *
   * @return Job completion status.
   */
  fun execute(): JobResultStatus
}