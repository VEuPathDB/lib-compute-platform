package org.veupathdb.lib.compute.platform.job

internal enum class PlatformJobResultStatus {
  Success,
  Failure,
  Aborted,
}

internal fun JobResultStatus.toPlatformStatus() =
  when (this) {
    JobResultStatus.Success -> PlatformJobResultStatus.Success
    JobResultStatus.Failure -> PlatformJobResultStatus.Failure
  }