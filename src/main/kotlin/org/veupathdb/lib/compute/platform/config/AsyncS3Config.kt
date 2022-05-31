package org.veupathdb.lib.compute.platform.config

/**
 * S3 Store Connection Configuration
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 *
 * @constructor Returns a new [AsyncS3Config] instance.
 *
 * @param host Hostname of the S3 store.
 *
 * @param port Connection port for the S3 store.
 *
 * Defaults to port `80`.
 *
 * @param https Whether HTTPS should be used when communicating with the S3
 * store.
 *
 * Defaults to `false`.
 *
 * @param bucket Name of the S3 bucket that will be operated on by this async
 * compute application.
 *
 * @param access Access token for the S3 store.
 *
 * @param secret Secret key for the S3 store.
 *
 * @param root Root 'directory' in which workspaces will be created.
 *
 * Defaults to the root of the bucket.
 */
class AsyncS3Config(
  internal val host:   String,
  internal val port:   Int = 80,
  internal val https:  Boolean = false,
  internal val bucket: String,
  internal val access: String,
  internal val secret: String,
  internal val root:   String = "/",
) {

  /**
   * Returns a new [AsyncS3Config] instance.
   *
   * @param host Hostname of the S3 store.
   *
   * @param bucket Name of the S3 bucket that will be operated on by this async
   * compute application.
   *
   * @param access Access token for the S3 store.
   *
   * @param secret Secret key for the S3 store.
   */
  constructor(host: String, bucket: String, access: String, secret: String) :
    this(host, 80, false, bucket, access, secret, "/")

  /**
   * Returns a new [AsyncS3Config] instance.
   *
   * @param host Hostname of the S3 store.
   *
   * @param bucket Name of the S3 bucket that will be operated on by this async
   * compute application.
   *
   * @param access Access token for the S3 store.
   *
   * @param secret Secret key for the S3 store.
   *
   * @param root Root 'directory' in which workspaces will be created.
   *
   * Defaults to the root of the bucket.
   */
  constructor(host: String, bucket: String, access: String, secret: String, root: String) :
    this(host, 80, false, bucket, access, secret, root)

  class Builder {
    var host:   String? = null
    var port:   Int = 80
    var https:  Boolean = false
    var bucket: String? = null
    var access: String? = null
    var secret: String? = null
    var root:   String = "/"

    /**
     * Sets the hostname for the S3 store.
     */
    fun host(h: String): Builder {
      host = h
      return this
    }

    /**
     * Sets the port number for the S3 store.
     */
    fun port(p: Int): Builder {
      port = p
      return this
    }

    /**
     * Sets the access token for the S3 store.
     */
    fun accessToken(u: String): Builder {
      access = u
      return this
    }

    /**
     * Sets the secret key for the S3 store.
     */
    fun secretKey(p: String): Builder {
      secret = p
      return this
    }

    /**
     * Sets the root 'directory' in which job workspaces will be created.
     */
    fun rootPath(r: String): Builder {
      root = r
      return this
    }

    /**
     * Sets whether HTTPS should be used when communicating with the S3 store.
     */
    fun https(h: Boolean): Builder {
      https = h
      return this
    }

    /**
     * Sets the name of the S3 bucket this application will operate on.
     */
    fun bucket(b: String): Builder {
      bucket = b
      return this
    }

    /**
     * Validates the contents of this builder and attempts to construct a new
     * [AsyncS3Config] instance.
     *
     * @return A new, configured [AsyncS3Config] instance.
     */
    fun build(): AsyncS3Config {
      return AsyncS3Config(
        host ?: throw IllegalStateException("Cannot build an AsyncS3Config instance with no hostname set!"),
        port,
        https,
        bucket ?: throw IllegalStateException("Cannot build an AsyncS3Config instance with no bucket set!"),
        access ?: throw IllegalStateException("Cannot build an AsyncS3Config instance with no access token set!"),
        secret ?: throw IllegalStateException("Cannot build an AsyncS3Config instance with no secret key set!"),
        root
      )
    }
  }

  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }
}