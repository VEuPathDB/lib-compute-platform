package org.veupathdb.lib.compute.platform.intern.queues

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.hash_id.HashID

internal object JobQueues {

  private var initialized = false

  private val queues = HashMap<String, QueueWrapper>()

  @JvmStatic
  internal fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to register queues more than once!")

    config.queues.forEach {
      queues[it.id] = QueueWrapper(it)
    }

    initialized = true
  }

  @JvmStatic
  fun submitJob(queue: String, jobID: HashID, rawConfig: JsonNode? = null) {
    if (queue !in queues)
      throw IllegalStateException("Attempted to submit job to unregistered queue '$queue'")

    queues[queue]!!.submitJob(jobID, rawConfig)
  }
}