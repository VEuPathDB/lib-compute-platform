package org.veupathdb.lib.compute.platform.job

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
   * Executes the job configured by the given [JobContext].
   *
   * @return Job completion status.
   */
  fun execute(ctx: JobContext): JobResult
}