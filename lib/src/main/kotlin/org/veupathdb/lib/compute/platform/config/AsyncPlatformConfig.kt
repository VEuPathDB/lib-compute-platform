package org.veupathdb.lib.compute.platform.config

private const val DefaultLocalWorkspaceRoot = "/tmp/workspaces"

/**
 * Async Compute Platform Configuration
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 *
 * @constructor Creates a new [AsyncPlatformConfig] instance.
 *
 * @param dbConfig Database configuration.
 *
 * @param s3Config S3 configuration.
 *
 * @param jobConfig Job execution configuration.
 *
 * @param queues Queue configurations.
 */
class AsyncPlatformConfig private constructor(
  internal val dbConfig: AsyncDBConfig,
  internal val s3Config: AsyncS3Config,
  internal val jobConfig: AsyncJobConfig,
  internal val queues: List<AsyncQueueConfig>,
  internal val localWorkspaceRoot: String,
) {

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
   * Async Platform Config Builder
   *
   * @author Elizabeth Paige Harper [https://github.com/foxcapades]
   * @since 1.0.0
   */
  class Builder {

    /**
     * Queue configurations.
     *
     * This list must contain at least one element by the time [build] is
     * called.
     */
    val queues = ArrayList<AsyncQueueConfig>(1)

    /**
     * Async DB Configuration.
     */
    var dbConfig: AsyncDBConfig? = null

    /**
     * Async S3 Configuration.
     */
    var s3Config: AsyncS3Config? = null

    /**
     * Async Job Configuration.
     */
    var jobConfig: AsyncJobConfig? = null

    /**
     * Local scratch workspace root path.
     */
    var localWorkspaceRoot: String = DefaultLocalWorkspaceRoot

    // region Queues

    /**
     * Adds the given queue configuration to this [Builder].
     */
    fun addQueue(conf: AsyncQueueConfig): Builder {
      queues.add(conf)
      return this
    }

    /**
     * Adds the given queue configuration to this [Builder].
     */
    @Suppress("unused")
    inline fun addQueue(fn: AsyncQueueConfig.Builder.() -> Unit) {
      queues.add(AsyncQueueConfig.build(fn))
    }

    /**
     * Adds the given queue configurations to this [Builder].
     */
    @Suppress("unused")
    fun addQueues(vararg conf: AsyncQueueConfig): Builder {
      conf.forEach(queues::add)
      return this
    }

    // endregion Queues

    // region Job Config

    fun jobConfig(conf: AsyncJobConfig): Builder {
      jobConfig = conf
      return this
    }

    @Suppress("unused")
    inline fun jobConfig(fn: AsyncJobConfig.Builder.() -> Unit) {
      jobConfig = AsyncJobConfig.build(fn)
    }

    // endregion Job Config

    // region DB Config

    fun dbConfig(conf: AsyncDBConfig): Builder {
      dbConfig = conf
      return this
    }

    @Suppress("unused")
    inline fun dbConfig(fn: AsyncDBConfig.Builder.() -> Unit) {
      dbConfig = AsyncDBConfig.build(fn)
    }

    // endregion DB Config

    // region S3 Config

    fun s3Config(conf: AsyncS3Config): Builder {
      s3Config = conf
      return this
    }

    @Suppress("unused")
    inline fun s3Config(fn: AsyncS3Config.Builder.() -> Unit) {
      s3Config = AsyncS3Config.build(fn)
    }

    // endregion S3 Config

    fun localWorkspaceRoot(path: String): Builder {
      localWorkspaceRoot = path
      return this
    }

    fun build(): AsyncPlatformConfig {
      if (queues.isEmpty())
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance with no queues configured!")

      if (jobConfig == null)
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance without a job config!")

      if (dbConfig == null)
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance without a DB config!")

      if (s3Config == null)
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance without an S3 config!")

      if (localWorkspaceRoot.isBlank())
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance with a blank local workspace root value!")

      return AsyncPlatformConfig(dbConfig!!, s3Config!!, jobConfig!!, queues, localWorkspaceRoot)
    }
  }
}
