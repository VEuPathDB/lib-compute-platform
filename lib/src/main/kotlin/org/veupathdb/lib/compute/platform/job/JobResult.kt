package org.veupathdb.lib.compute.platform.job

import org.veupathdb.lib.compute.platform.intern.JobResultImpl

/**
 * Job Execution Result
 *
 * Represents the results of the execution of a single job.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
interface JobResult {

  /**
   * Job Result Status
   */
  val status: JobResultStatus

  /**
   * Persistable job output files.
   *
   * The files in this list will be persisted to the backing S3 store (if they
   * exist).
   */
  val outputFiles: List<String>

  companion object {

    /**
     * Constructs a new [JobResult] for a job failure and marks the given list
     * of [files] as persistable.
     *
     * @param files List of files that can be persisted to the S3 store.
     *
     * @return A new, failed [JobResult].
     */
    @JvmStatic
    fun failure(vararg files: String): JobResult {
      return JobResultImpl(JobResultStatus.Failure, files.toList())
    }

    /**
     * Constructs a new [JobResult] for a job failure and marks the given list
     * of [files] as persistable.
     *
     * @param files List of files that can be persisted to the S3 store.
     *
     * @return A new, failed [JobResult].
     */
    @JvmStatic
    fun failure(files: Iterable<String>): JobResult {
      return JobResultImpl(JobResultStatus.Failure, files.toList())
    }

    /**
     * Constructs a new [JobResult] for a job success and marks the given list
     * of [files] as persistable.
     *
     * @param files List of files that can be persisted to the S3 store.
     *
     * @return A new, successful [JobResult].
     */
    @JvmStatic
    fun success(vararg files: String): JobResult {
      return JobResultImpl(JobResultStatus.Success, files.toList())
    }

    /**
     * Constructs a new [JobResult] for a job success and marks the given list
     * of [files] as persistable.
     *
     * @param files List of files that can be persisted to the S3 store.
     *
     * @return A new, successful [JobResult].
     */
    @JvmStatic
    fun success(files: Iterable<String>): JobResult {
      return JobResultImpl(JobResultStatus.Success, files.toList())
    }
  }
}
