package org.veupathdb.lib.compute.platform.config

class AsyncPlatformConfig private constructor(
  internal val dbConfig: AsyncDBConfig,
  internal val s3Config: AsyncS3Config,
  internal val jobConfig: AsyncJobConfig,
  internal val queues: List<AsyncQueueConfig>,
) {

  class Builder {
    private val queues = ArrayList<AsyncQueueConfig>(1)

    private var dbConfig: AsyncDBConfig? = null

    private var s3Config: AsyncS3Config? = null

    private var jobConfig: AsyncJobConfig? = null

    fun addQueue(conf: AsyncQueueConfig): Builder {
      queues.add(conf)
      return this
    }

    fun addQueues(vararg conf: AsyncQueueConfig): Builder {
      conf.forEach(queues::add)
      return this
    }

    fun jobConfig(conf: AsyncJobConfig): Builder {
      jobConfig = conf
      return this
    }

    fun dbConfig(conf: AsyncDBConfig): Builder {
      dbConfig = conf
      return this
    }

    fun s3Config(conf: AsyncS3Config): Builder {
      s3Config = conf
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

      return AsyncPlatformConfig(dbConfig!!, s3Config!!, jobConfig!!, queues)
    }
  }

  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }
}
