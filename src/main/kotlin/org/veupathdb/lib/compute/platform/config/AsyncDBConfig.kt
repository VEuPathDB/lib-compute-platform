package org.veupathdb.lib.compute.platform.config

class AsyncDBConfig(
  internal val host: String,
  internal val port: Int = 5432,
  internal val user: String,
  internal val pass: String,
  internal val name: String,
  internal val pool: Int = 10,
) {

  constructor(host: String, user: String, pass: String, name: String) :
    this(host, 5432, user, pass, name, 10)

  class Builder {
    var host: String? = null
    var port: Int     = 5432
    var user: String? = null
    var pass: String? = null
    var name: String? = null
    var pool: Int     = 10

    fun host(h: String): Builder {
      host = h
      return this
    }

    fun port(p: Int): Builder {
      port = p
      return this
    }

    fun username(u: String): Builder {
      user = u
      return this
    }

    fun password(p: String): Builder {
      pass = p
      return this
    }

    fun dbName(n: String): Builder {
      name = n
      return this
    }

    fun poolSize(s: Int): Builder {
      pool = s
      return this
    }

    fun build(): AsyncDBConfig {
      return AsyncDBConfig(
        host ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB hostname set!"),
        port,
        user ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB username set!"),
        pass ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB password set!"),
        name ?: throw IllegalStateException("Cannot construct an AsyncDBConfig instance with no DB name set!"),
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