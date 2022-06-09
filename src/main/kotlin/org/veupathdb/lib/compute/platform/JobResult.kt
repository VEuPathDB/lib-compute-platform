package org.veupathdb.lib.compute.platform

import org.veupathdb.lib.compute.platform.intern.JobResultImpl

interface JobResult {

  val status: JobResultStatus

  val outputFiles: List<String>

  companion object {
    @JvmStatic
    fun failure(vararg files: String): JobResult {
      return JobResultImpl(JobResultStatus.Failure, files.toList())
    }

    @JvmStatic
    fun failure(files: Iterable<String>): JobResult {
      return JobResultImpl(JobResultStatus.Failure, files.toList())
    }

    @JvmStatic
    fun success(vararg files: String): JobResult {
      return JobResultImpl(JobResultStatus.Success, files.toList())
    }

    @JvmStatic
    fun success(files: Iterable<String>): JobResult {
      return JobResultImpl(JobResultStatus.Success, files.toList())
    }
  }
}
