package org.veupathdb.lib.compute.platform.config

import org.veupathdb.lib.compute.platform.job.JobExecutor
import org.veupathdb.lib.compute.platform.job.JobExecutorFactory

private const val DefaultExpirationDays = 30

/**
 * Async Job Processing Configuration
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
class AsyncJobConfig {

  /**
   * Job Executor Factory
   *
   * Used to provide [JobExecutor] instances that will execute individual jobs.
   */
  internal val executorFactory: JobExecutorFactory

  /**
   * Number of days after a job was last accessed that it may be expired and
   * pruned.
   */
  internal val expirationDays: Int

  /**
   * Creates a new [AsyncJobConfig] instance.
   *
   * @param executorFactory Provider for [JobExecutor] instances that will be
   * used to process individual jobs.
   */
  constructor(executorFactory: JobExecutorFactory) :
    this(executorFactory, DefaultExpirationDays)

  /**
   * Creates a new [AsyncJobConfig] instance.
   *
   * @param executorFactory Provider for [JobExecutor] instances that will be
   * used to process individual jobs.
   *
   * @param expirationDays Number of days after a job was last accessed that it
   * may be expired and pruned.
   */
  constructor(executorFactory: JobExecutorFactory, expirationDays: Int) {
    this.executorFactory = executorFactory
    this.expirationDays = expirationDays
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
   * Async Job Config Builder
   *
   * @author Elizabeth Paige Harper [https://github.com/foxcapades]
   * @since 1.0.0
   */
  class Builder {

    /**
     * Job Executor Factory
     *
     * Used to provide [JobExecutor] instances that will execute individual jobs.
     */
    var executorFactory: JobExecutorFactory? = null

    /**
     * Number of days after a job was last accessed that it may be expired and
     * pruned.
     */
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