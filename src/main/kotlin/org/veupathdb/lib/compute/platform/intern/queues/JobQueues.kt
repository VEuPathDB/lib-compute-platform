package org.veupathdb.lib.compute.platform.intern.queues

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.hash_id.HashID

/**
 * Job Queue Manager
 *
 * Provides methods for interacting with the platform's job queues.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
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

  /**
   * Tests whether a queue exists with the given name/ID.
   *
   * @param queue Name/ID of the queue to test for.
   *
   * @return `true` if a queue exists with the given name/ID, otherwise `false`.
   */
  @JvmStatic
  operator fun contains(queue: String): Boolean {
    Log.trace("testing for the existence of queue {} in job queue manager", queue)
    return queue in queues
  }

  /**
   * Submits a new job to the target queue.
   *
   * @param queue Name/ID of the queue the job should be submitted to.
   *
   * @param jobID Hash ID of the job to be submitted.
   *
   * @param rawConfig Optional, raw, json configuration for the job being
   * submitted.
   *
   * @throws IllegalArgumentException If the target [queue] does not exist.
   */
  @JvmStatic
  fun submitJob(queue: String, jobID: HashID, rawConfig: JsonNode?) {
    Log.debug("submitting job {} to job queue {}", jobID, queue)

    if (queue !in queues)
      throw IllegalArgumentException("Attempted to submit job to unregistered queue '$queue'")

    queues[queue]!!.submitJob(jobID, rawConfig)
  }
}