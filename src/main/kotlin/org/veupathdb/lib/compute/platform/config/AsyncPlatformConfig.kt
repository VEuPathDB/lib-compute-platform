package org.veupathdb.lib.compute.platform.config

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
) {

  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }

  class Builder {
    val queues = ArrayList<AsyncQueueConfig>(1)

    var dbConfig: AsyncDBConfig? = null

    var s3Config: AsyncS3Config? = null

    var jobConfig: AsyncJobConfig? = null

    // region Queues

    fun addQueue(conf: AsyncQueueConfig): Builder {
      queues.add(conf)
      return this
    }

    inline fun addQueue(fn: AsyncQueueConfig.Builder.() -> Unit) {
      queues.add(AsyncQueueConfig.build(fn))
    }

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

    inline fun jobConfig(fn: AsyncJobConfig.Builder.() -> Unit) {
      jobConfig = AsyncJobConfig.build(fn)
    }

    // endregion Job Config

    // region DB Config

    fun dbConfig(conf: AsyncDBConfig): Builder {
      dbConfig = conf
      return this
    }

    inline fun dbConfig(fn: AsyncDBConfig.Builder.() -> Unit) {
      dbConfig = AsyncDBConfig.build(fn)
    }

    // endregion DB Config

    // region S3 Config

    fun s3Config(conf: AsyncS3Config): Builder {
      s3Config = conf
      return this
    }

    inline fun s3Config(fn: AsyncS3Config.Builder.() -> Unit) {
      s3Config = AsyncS3Config.build(fn)
    }

    // endregion S3 Config

    fun build(): AsyncPlatformConfig {
      if (queues.isEmpty())
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance with no queues configured!")

      if (jobConfig == null)
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance without a job config!")

      if (dbConfig == null)
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance without a DB config!")

      if (s3Config == null)
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance without an S3 config!")

      return AsyncPlatformConfig(dbConfig!!, s3Config!!, jobConfig!!, queues)
    }
  }
}
