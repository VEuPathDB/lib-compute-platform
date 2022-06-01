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
 * @param user Connection credentials username.
 *
 * @param pass Connection credentials password.
 *
 * @param name PostgreSQL database name.
 *
 * @param pool Max connection pool size.
 *
 * Defaults to `10`.
 */
class AsyncDBConfig @JvmOverloads constructor(
  internal val name: String,
  internal val user: String,
  internal val pass: String,
  internal val host: String,
  internal val port: Int = 5432,
  internal val pool: Int = 10,
) {

  class Builder {
    var host: String? = null
    var port: Int     = 5432
    var user: String? = null
    var pass: String? = null
    var name: String? = null
    var pool: Int     = 10

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
      user = u
      return this
    }

    /**
     * Sets the connection credentials password.
     */
    fun password(p: String): Builder {
      pass = p
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
      pool = s
      return this
    }

    fun build(): AsyncDBConfig {
      return AsyncDBConfig(
        name ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB name set!"),
        user ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB username set!"),
        pass ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB password set!"),
        host ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB hostname set!"),
        port,
        pool
      )
    }
  }

  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }
}