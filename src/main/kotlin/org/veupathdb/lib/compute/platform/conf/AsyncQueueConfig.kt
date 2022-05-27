package org.veupathdb.lib.compute.platform.conf

/**
 * Configuration entry for a single RabbitMQ queue.
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
 * Defaults to `5672`.
 *
 * @param workers Number of worker threads to be used by consumers of the target
 * queue.
 *
 * Defaults to `10`.
 */
data class AsyncQueueConfig @JvmOverloads constructor(
  internal val id: String,
  internal val username: String,
  internal val password: String,
  internal val host: String,
  internal val port: Int = 5672,
  internal val workers: Int = 5,
)