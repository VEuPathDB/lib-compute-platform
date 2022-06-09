package org.veupathdb.lib.compute.platform.config

import org.veupathdb.lib.compute.platform.JobExecutor
import org.veupathdb.lib.compute.platform.JobExecutorFactory

/**
 * Async Job Processing Configuration
 *
 * @constructor Creates a new [AsyncJobConfig] instance.
 *
 * @param executorFactory Provider for [JobExecutor] instances that will be used
 * to process individual jobs.
 */
class AsyncJobConfig private constructor(
  internal val executorFactory: JobExecutorFactory,
  internal val expirationDays: Int = 30
) {
  /**
   * Creates a new [AsyncJobConfig] instance.
   *
   * @param executorFactory Provider for [JobExecutor] instances that will be used
   * to process individual jobs.
   *
   * @param persistableFiles List of files that, if present in a job workspace on
   * job completion, will be persisted to the S3 store.
   *
   * If this list is empty, all files that exist in a job's scratch space on job
   * completion will be persisted.
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