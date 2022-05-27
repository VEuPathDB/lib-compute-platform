package org.veupathdb.lib.compute.platform.job

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.conf.AsyncPlatformConfig
import org.veupathdb.lib.hash_id.HashID

internal object JobExecutors {

  private var initialized = false

  private var provider: JobExecutorFactory? = null

  @JvmStatic
  fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize JobExecutors more than once!")

    provider = config.jobExecutorFactory

    initialized = true
  }

  fun new(jobID: HashID, rawConfig: JsonNode?): JobExecutor {
    if (provider == null)
      throw IllegalStateException("Attempted to execute a job before platform initialization!")

    return provider!!.newJobExecutor(jobID, rawConfig)
  }
}