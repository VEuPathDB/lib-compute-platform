package org.veupathdb.lib.compute.platform.job

/**
 * Async Job Executor
 *
 * Executes a configured job.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
@FunctionalInterface
fun interface JobExecutor {

  /**
   * Executes the job configured by the given [JobContext].
   *
   * @param ctx Context in/for the job that is to be executed.
   *
   * @return Job completion status.
   */
  fun execute(ctx: JobContext): JobResult
}