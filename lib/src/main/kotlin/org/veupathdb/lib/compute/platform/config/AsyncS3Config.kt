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
 * @param accessToken Access token for the S3 store.
 *
 * @param secretKey Secret key for the S3 store.
 *
 * @param rootPath Root 'directory' in which workspaces will be created.
 *
 * Defaults to the root of the bucket.
 */
open class AsyncS3Config(
  internal val host: String,
  internal val port: Int,
  internal val https: Boolean,
  internal val bucket: String,
  internal val accessToken: String,
  internal val secretKey: String,
  internal val rootPath: String,
) {
  companion object {
    protected const val DefaultPort = 80
    protected const val DefaultHTTPS = false
    protected const val DefaultRootPath = "/"

    @JvmStatic
    @Suppress("unused")
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }

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
  @Suppress("unused")
  constructor(host: String, bucket: String, access: String, secret: String) :
    this(host, DefaultPort, DefaultHTTPS, bucket, access, secret, DefaultRootPath)

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
  @Suppress("unused")
  constructor(host: String, bucket: String, access: String, secret: String, root: String) :
    this(host, DefaultPort, DefaultHTTPS, bucket, access, secret, root)

  open class Builder {

    var host: String? = null

    var port: Int = 80

    var https: Boolean = false

    var bucket: String? = null

    var accessToken: String? = null

    var secretKey: String? = null

    var rootPath: String = "/"

    /**
     * Sets the hostname for the S3 store.
     */
    @Suppress("unused")
    fun host(h: String): Builder {
      host = h
      return this
    }

    /**
     * Sets the port number for the S3 store.
     */
    @Suppress("unused")
    fun port(p: Int): Builder {
      port = p
      return this
    }

    /**
     * Sets the access token for the S3 store.
     */
    @Suppress("unused")
    fun accessToken(u: String): Builder {
      accessToken = u
      return this
    }

    /**
     * Sets the secret key for the S3 store.
     */
    @Suppress("unused")
    fun secretKey(p: String): Builder {
      secretKey = p
      return this
    }

    /**
     * Sets the root 'directory' in which job workspaces will be created.
     */
    @Suppress("unused")
    fun rootPath(r: String): Builder {
      rootPath = r
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
    @Suppress("unused")
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
    open fun build(): AsyncS3Config {
      // We check null and blank because these are likely coming from env vars
      // and docker compose will set blank values for vars defined in the
      // docker-compose.yml file.

      if (host == null)
        throw IllegalStateException("Cannot build an AsyncS3Config instance with a null hostname!")
      if (host!!.isBlank())
        throw IllegalStateException("Cannot build an AsyncS3Config instance with a blank hostname!")

      if (bucket == null)
        throw IllegalStateException("Cannot build an AsyncS3Config instance with a null bucket!")
      if (bucket!!.isBlank())
        throw IllegalStateException("Cannot build an AsyncS3Config instance with a blank bucket!")

      if (accessToken == null)
        throw IllegalStateException("Cannot build an AsyncS3Config instance with a null access token!")
      if (accessToken!!.isBlank())
        throw IllegalStateException("Cannot build an AsyncS3Config instance with a blank access token!")

      if (secretKey == null)
        throw IllegalStateException("Cannot build an AsyncS3Config instance with a null secret key!")
      if (secretKey!!.isBlank())
        throw IllegalStateException("Cannot build an AsyncS3Config instance with a blank secret key!")

      return AsyncS3Config(host!!, port, https, bucket!!, accessToken!!, secretKey!!, rootPath)
    }
  }
}