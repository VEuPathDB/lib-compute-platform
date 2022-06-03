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
 *
 * @param persistableFiles List of files that, if present in a job workspace on
 * job completion, will be persisted to the S3 store.
 *
 * If this list is empty, all files that exist in a job's scratch space on job
 * completion will be persisted.
 */
class AsyncJobConfig @JvmOverloads constructor(
  internal val executorFactory: JobExecutorFactory,
  internal val persistableFiles: List<String> = emptyList(),
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
  constructor(executorFactory: JobExecutorFactory, vararg persistableFiles: String) :
    this(executorFactory, persistableFiles.asList())

  class Builder {

    var executorFactory: JobExecutorFactory? = null

    var persistableFiles = ArrayList<String>(5)

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
     * Appends the given collection of files to this [Builder]'s
     * [persistableFiles] list.
     *
     * Persistable files are files that, if present in a job workspace on job
     * completion, will be persisted to the S3 store.
     *
     * If the configured list is empty, all files that exist in a job's scratch
     * space on job completion will be persisted.
     */
    fun persistableFiles(files: Iterable<String>): Builder {
      persistableFiles.addAll(files)
      return this
    }

    /**
     * Appends the given files to this [Builder]'s [persistableFiles] list.
     *
     * Persistable files are files that, if present in a job workspace on job
     * completion, will be persisted to the S3 store.
     *
     * If the configured list is empty, all files that exist in a job's scratch
     * space on job completion will be persisted.
     */
    fun persistableFiles(vararg files: String): Builder {
      persistableFiles.addAll(files)
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
      return AsyncJobConfig(
        executorFactory ?: throw IllegalStateException("Cannot build an AsyncJobConfig instance with no executor factory set!"),
        if (persistableFiles.isEmpty()) emptyList() else persistableFiles,
        expirationDays
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