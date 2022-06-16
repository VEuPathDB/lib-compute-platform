package org.veupathdb.lib.compute.platform.intern.jobs

import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.job.JobExecutorContext
import org.veupathdb.lib.compute.platform.job.JobExecutorFactory

internal object JobExecutors {

  private var initialized = false

  private var provider: JobExecutorFactory? = null

  @JvmStatic
  fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize JobExecutors more than once!")

    initialized = true

    provider = config.jobConfig.executorFactory
  }

  fun new(ctx: JobExecutorContext): JobExecutionHandler {
    if (provider == null)
      throw IllegalStateException("Attempted to execute a job before platform initialization!")

    return JobExecutionHandler(provider!!.newJobExecutor(ctx))
  }
}