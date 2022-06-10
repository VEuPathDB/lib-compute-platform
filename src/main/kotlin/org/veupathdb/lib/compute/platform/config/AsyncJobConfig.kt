package org.veupathdb.lib.compute.platform.config

import org.veupathdb.lib.compute.platform.JobExecutor
import org.veupathdb.lib.compute.platform.JobExecutorFactory

/**
 * Async Job Processing Configuration
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 *
 * @constructor Creates a new [AsyncJobConfig] instance.
 *
 * @param executorFactory Provider for [JobExecutor] instances that will be used
 * to process individual jobs.
 */
class AsyncJobConfig(
  internal val executorFactory: JobExecutorFactory,
  internal val expirationDays: Int = 30
) {

  /**
   * Creates a new [AsyncJobConfig] instance.
   *
   * @param executorFactory Provider for [JobExecutor] instances that will be used
   * to process individual jobs.
   */
  constructor(executorFactory: JobExecutorFactory) :
    this(executorFactory, 30)


  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }

  class Builder {

    var executorFactory: JobExecutorFactory? = null

    var expirationDays = 30

    /**
     * Sets the provider for [JobExecutor] instances that will be used to
     * process individual jobs.
     */
    fun executorFactory(fac: JobExecutorFactory): Builder {
      executorFactory = fac
      return this
    }

    /**
     * Sets the number of days a job can go without being accessed before being
     * considered expired and pruned.
     */
    fun expirationDays(d: Int): Builder {
      expirationDays = d
      return this
    }

    fun build(): AsyncJobConfig {
      if (executorFactory == null)
        throw IllegalStateException("Cannot build an AsyncJobConfig instance with no executor factory set!")

      return AsyncJobConfig(executorFactory!!, expirationDays)
    }
  }
}