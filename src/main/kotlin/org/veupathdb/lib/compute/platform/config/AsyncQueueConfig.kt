package org.veupathdb.lib.compute.platform.config

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
class AsyncQueueConfig @JvmOverloads constructor(
  internal val id: String,
  internal val username: String,
  internal val password: String,
  internal val host: String,
  internal val port: Int = 5672,
  internal val workers: Int = 5,
) {

  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }

  class Builder {

    var id: String? = null

    var username: String? = null

    var password: String? = null

    var host: String? = null

    var port = 5672

    var workers = 5

    fun id(id: String): Builder {
      this.id = id
      return this
    }

    fun username(username: String): Builder {
      this.username = username
      return this
    }

    fun password(password: String): Builder {
      this.password = password
      return this
    }

    fun host(host: String): Builder {
      this.host = host
      return this
    }

    fun port(port: Int): Builder {
      this.port = port
      return this
    }

    fun workers(workers: Int): Builder {
      this.workers = workers
      return this
    }

    fun build(): AsyncQueueConfig {
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

      return AsyncQueueConfig(id!!, username!!, password!!, host!!, port, workers)
    }
  }
}