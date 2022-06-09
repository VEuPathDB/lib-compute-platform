package org.veupathdb.lib.compute.platform.intern

import org.veupathdb.lib.compute.platform.JobResult
import org.veupathdb.lib.compute.platform.JobResultStatus

internal data class JobResultImpl(
  override val status: JobResultStatus,
  override val outputFiles: List<String>
) : JobResult