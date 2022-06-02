package org.veupathdb.lib.compute.platform.intern.queues

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.intern.db.model.JobRecord
import org.veupathdb.lib.hash_id.HashID
import java.util.stream.Stream

internal object JobQueues {

  private val Log = LoggerFactory.getLogger(javaClass)

  private var initialized = false

  private val queues = HashMap<String, QueueWrapper>()

  @JvmStatic
  internal fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to register queues more than once!")

    initialized = true

    Log.debug("initializing job queue manager")

    config.queues.forEach {
      queues[it.id] = QueueWrapper(it)
    }
  }

  @JvmStatic
  operator fun contains(queue: String): Boolean {
    Log.trace("testing for the existence of queue {} in job queue manager", queue)
    return queue in queues
  }

  @JvmStatic
  fun submitJob(queue: String, jobID: HashID, rawConfig: JsonNode? = null) {
    Log.debug("submitting job {} to job queue {}", jobID, queue)

    if (queue !in queues)
      throw IllegalStateException("Attempted to submit job to unregistered queue '$queue'")

    queues[queue]!!.submitJob(jobID, rawConfig)
  }
}