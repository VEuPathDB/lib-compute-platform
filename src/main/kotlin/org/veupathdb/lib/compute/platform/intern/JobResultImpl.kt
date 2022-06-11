package org.veupathdb.lib.compute.platform.intern

import org.veupathdb.lib.compute.platform.job.JobResult
import org.veupathdb.lib.compute.platform.job.JobResultStatus

internal data class JobResultImpl(
  override val status: JobResultStatus,
  override val outputFiles: List<String>
) : JobResult