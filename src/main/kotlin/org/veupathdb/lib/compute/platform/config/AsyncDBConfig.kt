package org.veupathdb.lib.compute.platform.config

/**
 * Async Platform Database Connection Configuration
 *
 * Configures how the async compute platform library will connect to its managed
 * PostgreSQL instance.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 *
 * @constructor Creates a new [AsyncDBConfig] instance.
 *
 * @param host PostgreSQL database hostname.
 *
 * @param port PostgreSQL host port.
 *
 * Defaults to `5432`.
 *
 * @param username Connection credentials username.
 *
 * @param password Connection credentials password.
 *
 * @param name PostgreSQL database name.
 *
 * @param poolSize Max connection pool size.
 *
 * Defaults to `10`.
 */
class AsyncDBConfig @JvmOverloads constructor(
  internal val name: String,
  internal val username: String,
  internal val password: String,
  internal val host: String,
  internal val port: Int = 5432,
  internal val poolSize: Int = 10,
) {

  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }

  class Builder {

    var host: String? = null

    var port: Int = 5432

    var username: String? = null

    var password: String? = null

    var name: String? = null

    var poolSize: Int = 10

    /**
     * Sets the hostname setting for the PostgreSQL database.
     */
    fun host(h: String): Builder {
      host = h
      return this
    }

    /**
     * Sets the port setting for the PostgreSQL database.
     */
    fun port(p: Int): Builder {
      port = p
      return this
    }

    /**
     * Sets the connection credentials username.
     */
    fun username(u: String): Builder {
      username = u
      return this
    }

    /**
     * Sets the connection credentials password.
     */
    fun password(p: String): Builder {
      password = p
      return this
    }

    /**
     * Sets the PostgreSQL database name.
     */
    fun dbName(n: String): Builder {
      name = n
      return this
    }

    /**
     * Sets the max pool size.
     */
    fun poolSize(s: Int): Builder {
      poolSize = s
      return this
    }

    fun build(): AsyncDBConfig {
      return AsyncDBConfig(
        name ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB name set!"),
        username ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB username set!"),
        password ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB password set!"),
        host ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB hostname set!"),
        port,
        poolSize
      )
    }
  }

}