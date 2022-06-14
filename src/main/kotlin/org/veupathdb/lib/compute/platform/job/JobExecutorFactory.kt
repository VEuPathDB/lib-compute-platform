package org.veupathdb.lib.compute.platform.job


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
   * @param ctx Context for which a new [JobExecutor] instance is being
   * requested.
   *
   * @return A new [JobExecutor] instance.
   */
  fun newJobExecutor(ctx: JobExecutorContext): JobExecutor
}