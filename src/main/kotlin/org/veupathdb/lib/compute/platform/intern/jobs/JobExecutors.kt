package org.veupathdb.lib.compute.platform.intern.jobs

import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.JobExecutorFactory

internal object JobExecutors {

  private var initialized = false

  private var provider: JobExecutorFactory? = null

  private var persistables: List<String> = emptyList()

  @JvmStatic
  fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize JobExecutors more than once!")

    initialized = true

    provider = config.jobConfig.executorFactory

    persistables = config.jobConfig.persistableFiles
  }

  fun new(): JobExecutionHandler {
    if (provider == null)
      throw IllegalStateException("Attempted to execute a job before platform initialization!")

    return JobExecutionHandler(provider!!.newJobExecutor(), persistables)
  }
}