package org.veupathdb.lib.compute.platform.config

private const val DefaultPort = 5432
private const val DefaultPoolSize = 10

/**
 * Async Platform Database Connection Configurationn
 *
 * Configures how the async compute platform library will connect to its managed
 * PostgreSQL instance.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
class AsyncDBConfig {

  /**
   * Database name.
   */
  internal val dbName: String

  /**
   * Credentials username.
   */
  internal val username: String

  /**
   * Credentials password.
   */
  internal val password: String

  /**
   * Database instance host.
   */
  internal val host: String

  /**
   * Database instance host port.
   */
  internal val port: Int

  /**
   * Max connection pool size.
   */
  internal val poolSize: Int

  /**
   * Creates a new [AsyncDBConfig] instance.
   *
   * @param host PostgreSQL database hostname.
   *
   * @param username Connection credentials username.
   *
   * @param password Connection credentials password.
   *
   * @param dbName PostgreSQL database name.
   */
  constructor(dbName: String, username: String, password: String, host: String) :
    this(dbName, username, password, host, DefaultPort, DefaultPoolSize)

  /**
   * Creates a new [AsyncDBConfig] instance.
   *
   * @param host PostgreSQL database hostname.
   *
   * @param username Connection credentials username.
   *
   * @param password Connection credentials password.
   *
   * @param dbName PostgreSQL database name.
   *
   * @param poolSize Max connection pool size.
   *
   * Defaults to `10`.
   */
  constructor(dbName: String, username: String, password: String, host: String, poolSize: Int) :
    this(dbName, username, password, host, DefaultPort, poolSize)

  /**
   * Creates a new [AsyncDBConfig] instance.
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
   * @param dbName PostgreSQL database name.
   *
   * @param poolSize Max connection pool size.
   *
   * Defaults to `10`.
   */
  constructor(dbName: String, username: String, password: String, host: String, port: Int, poolSize: Int) {
    this.dbName = dbName
    this.username = username
    this.password = password
    this.host = host
    this.port = port
    this.poolSize = poolSize
  }

  companion object {
    /**
     * Creates and returns a new [Builder] instance.
     */
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }

  /**
   * Async DB Config Builder
   *
   * @author Elizabeth Paige Harper [https://github.com/foxcapades]
   * @since 1.0.0
   */
  class Builder {

    /**
     * Database instance hostname.
     */
    var host: String? = null

    /**
     * Database instance host port.
     */
    var port: Int = DefaultPort

    /**
     * Credentials username.
     */
    var username: String? = null

    /**
     * Credentials password.
     */
    var password: String? = null

    /**
     * Database name.
     */
    var dbName: String? = null

    /**
     * Max database connection pool size.
     */
    var poolSize: Int = DefaultPoolSize

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
      dbName = n
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
      // We check null and blank because these are likely coming from env vars
      // and docker compose will set blank values for vars defined in the
      // docker-compose.yml file.

      if (dbName == null)
        throw IllegalStateException("Cannot construct an AsyncDBConfig instance with a null DB name!")
      if (dbName!!.isBlank())
        throw IllegalStateException("Cannot construct an AsyncDBConfig instance with a blank DB name!")

      if (username == null)
        throw IllegalStateException("Cannot construct an AsyncDBConfig instance with a null DB username!")
      if (username!!.isBlank())
        throw IllegalStateException("Cannot construct an AsyncDBConfig instance with a blank DB username!")

      if (password == null)
        throw IllegalStateException("Cannot construct an AsyncDBConfig instance with a null DB password!")
      if (password!!.isBlank())
        throw IllegalStateException("Cannot construct an AsyncDBConfig instance with a blank DB password!")

      if (host == null)
        throw IllegalStateException("Cannot construct an AsyncDBConfig instance with a null DB host!")
      if (host!!.isBlank())
        throw IllegalStateException("Cannot construct an AsyncDBConfig instance with a blank DB host!")


      return AsyncDBConfig(dbName!!, username!!, password!!, host!!, port, poolSize)
    }
  }

}
