package org.veupathdb.lib.compute.platform.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val DefaultRabbitMQPort = 5672
private const val DefaultWorkerCount  = 5
private const val DefaultMessageAckTimeoutMinutes = 30

/**
 * Configuration entry for a single RabbitMQ queue.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 *
 * @constructor Constructs a new [AsyncQueueConfig] instance.
 *
 * @param id Unique identifier for the queue.
 *
 * This value will be used to reference the queue when submitting jobs to it.
 *
 * @param username RabbitMQ credentials username.
 *
 * @param password RabbitMQ credentials password.
 *
 * @param host Hostname for the target RabbitMQ instance.
 *
 * @param port Port number for connecting to the target RabbitMQ instance.
 *
 * Default value is `5672`.
 *
 * @param workers Number of worker threads to be used by consumers of the target
 * queue.
 *
 * Default value is `5`.
 *
 * @param messageAckTimeout Timeout window in which a queue message must be
 * acknowledged.  RabbitMQ will kill channels on which a message has not been
 * acknowledged within this window.
 *
 * This value *MUST* be at least 5 minutes.
 *
 * Default value is 30 minutes.
 */
class AsyncQueueConfig(
  internal val id: String,
  internal val username: String,
  internal val password: String,
  internal val host: String,
  internal val port: Int,
  internal val workers: Int,
  internal val messageAckTimeout: Duration,
) {

  /**
   * Constructs a new [AsyncQueueConfig] instance.
   *
   * The [port] and [workers] properties will be assigned default values.
   *
   * @param id Unique identifier for the queue.
   *
   * This value will be used to reference the queue when submitting jobs to it.
   *
   * @param username RabbitMQ credentials username.
   *
   * @param password RabbitMQ credentials password.
   *
   * @param host Hostname for the target RabbitMQ instance.
   */
  constructor(id: String, username: String, password: String, host: String) :
    this(id, username, password, host, DefaultRabbitMQPort, DefaultWorkerCount, DefaultMessageAckTimeoutMinutes.minutes)

  /**
   * Constructs a new [AsyncQueueConfig] instance.
   *
   * The [port] property will be assigned a default value.
   *
   * @param id Unique identifier for the queue.
   *
   * This value will be used to reference the queue when submitting jobs to it.
   *
   * @param username RabbitMQ credentials username.
   *
   * @param password RabbitMQ credentials password.
   *
   * @param host Hostname for the target RabbitMQ instance.
   *
   * @param workers Number of worker threads to be used by consumers of the target
   * queue.
   *
   * Default value is `5`.
   */
  constructor(id: String, username: String, password: String, host: String, workers: Int) :
    this(id, username, password, host, DefaultRabbitMQPort, workers, DefaultMessageAckTimeoutMinutes.minutes)

  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }

  /**
   * Async Queue Config Builder
   *
   * Builder class for constructing a new [AsyncQueueConfig] instance.
   *
   * @author Elizabeth Paige Harper [https://github.com/foxcapades]
   * @since 1.0.0
   */
  class Builder {

    /**
     * Unique identifier for the queue.
     *
     * This value will be used to reference the queue when submitting jobs to it.
     */
    var id: String? = null

    /**
     * RabbitMQ credentials username.
     */
    var username: String? = null

    /**
     * RabbitMQ credentials password.
     */
    var password: String? = null

    /**
     * Hostname for the target RabbitMQ instance.
     */
    var host: String? = null

    /**
     * Port number for connecting to the target RabbitMQ instance.
     *
     * Default value is `5672`.
     */
    var port = DefaultRabbitMQPort

    /**
     * Number of worker threads to be used by consumers of the target
     * queue.
     *
     * Default value is `5`.
     */
    var workers = DefaultWorkerCount

    /**
     * Message acknowledgement timeout value to set for this queue.
     *
     * This value should be long enough to accommodate the longest expected job
     * runtimes for the queue.  RabbitMQ itself will disconnect the queue if a
     * message is not acknowledged within the configured timeout window.
     *
     * Default value is 30 minutes.
     */
    var messageAckTimeout = DefaultMessageAckTimeoutMinutes.minutes

    /**
     * Sets the unique identifier for the queue.
     */
    fun id(id: String): Builder {
      this.id = id
      return this
    }

    /**
     * Sets the RabbitMQ credentials username for the queue.
     */
    fun username(username: String): Builder {
      this.username = username
      return this
    }

    /**
     * Sets the RabbitMQ credentials password for the queue.
     */
    fun password(password: String): Builder {
      this.password = password
      return this
    }

    /**
     * Sets the RabbitMQ hostname for the queue.
     */
    fun host(host: String): Builder {
      this.host = host
      return this
    }

    /**
     * Sets the RabbitMQ host port for the queue.
     */
    fun port(port: Int): Builder {
      this.port = port
      return this
    }

    /**
     * Sets the number of worker threads to use as consumers for this queue.
     */
    fun workers(workers: Int): Builder {
      this.workers = workers
      return this
    }

    /**
     * Sets the [messageAckTimeout] value to the given duration.
     */
    fun messageAckTimeout(timeout: Duration) = apply { this.messageAckTimeout = timeout }

    /**
     * Sets the [messageAckTimeout] value to a duration of the given value in
     * minutes.
     */
    fun messageAckTimeoutMinutes(timeout: Int) = apply { this.messageAckTimeout = timeout.minutes }

    fun build(): AsyncQueueConfig {
      // We check null and blank because these are likely coming from env vars
      // and docker compose will set blank values for vars defined in the
      // docker-compose.yml file.

      if (id == null)
        throw IllegalStateException("Cannot build an AsyncQueueConfig instance with a null id!")
      if (id!!.isBlank())
        throw IllegalStateException("Cannot build an AsyncQueueConfig instance with a blank id!")

      if (username == null)
        throw IllegalStateException("Cannot build an AsyncQueueConfig instance with a null username!")
      if (username!!.isBlank())
        throw IllegalStateException("Cannot build an AsyncQueueConfig instance with a blank username!")

      if (password == null)
        throw IllegalStateException("Cannot build an AsyncQueueConfig instance with a null password!")
      if (password!!.isBlank())
        throw IllegalStateException("Cannot build an AsyncQueueConfig instance with a blank password!")

      if (host == null)
        throw IllegalStateException("Cannot build an AsyncQueueConfig instance with a null host!")
      if (host!!.isBlank())
        throw IllegalStateException("Cannot build an AsyncQueueConfig instance with a blank host!")

      if (messageAckTimeout < 5.minutes)
        throw IllegalStateException("Message ack timeout values less than 5 minutes are likely to cause undefined" +
          " behavior in RabbitMQ")

      return AsyncQueueConfig(id!!, username!!, password!!, host!!, port, workers, messageAckTimeout)
    }
  }
}