package org.veupathdb.lib.compute.platform

class AsyncPlatformConfig private constructor(
  internal val queues: List<AsyncQueueConfig>,
  internal val jobExecutorFactory: JobExecutorFactory,
) {

  class Builder {
    private val queues = ArrayList<AsyncQueueConfig>(1)

    private var jobExecutorFactory: JobExecutorFactory? = null

    fun addQueue(conf: AsyncQueueConfig): Builder {
      queues.add(conf)
      return this
    }

    fun jobExecFactory(fac: JobExecutorFactory): Builder {
      jobExecutorFactory = fac
      return this
    }

    fun build(): AsyncPlatformConfig {
      if (queues.isEmpty())
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance with no queues configured!")

      if (jobExecutorFactory == null)
        throw IllegalStateException("Cannot build an AsyncPlatformConfig instance without a job executor factory!")

      return AsyncPlatformConfig(queues, jobExecutorFactory!!)
    }
  }

  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }
}
