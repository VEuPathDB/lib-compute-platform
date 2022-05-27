package org.veupathdb.lib.compute.platform

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.conf.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.job.JobExecutors
import org.veupathdb.lib.compute.platform.queues.JobQueues
import org.veupathdb.lib.hash_id.HashID

object AsyncPlatform {

  private var initialized = false

  @JvmStatic
  fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize AsyncPlatform more than once!")

    JobQueues.init(config)
    JobExecutors.init(config)

    initialized = true
  }

  @JvmStatic
  @JvmOverloads
  fun submitJob(queue: String, jobID: HashID, rawConfig: JsonNode? = null) {
    JobQueues.submitJob(queue, jobID, rawConfig)
  }
}