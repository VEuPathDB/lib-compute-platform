package org.veupathdb.lib.compute.platform.intern.jobs

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.job.JobExecutorFactory
import org.veupathdb.lib.hash_id.HashID

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

  fun new(jobID: HashID, config: JsonNode? = null): JobExecutionHandler {
    if (provider == null)
      throw IllegalStateException("Attempted to execute a job before platform initialization!")

    return JobExecutionHandler(provider!!.newJobExecutor(jobID, config))
  }
}