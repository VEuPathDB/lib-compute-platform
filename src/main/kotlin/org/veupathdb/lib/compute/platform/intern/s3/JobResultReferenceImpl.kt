package org.veupathdb.lib.compute.platform.intern.s3

import org.veupathdb.lib.compute.platform.job.JobResultReference
import org.veupathdb.lib.s3.workspaces.WorkspaceFile
import java.io.InputStream

internal data class JobResultReferenceImpl(private val raw: WorkspaceFile) : JobResultReference {
  override val name: String
    get() = raw.name

  override val size: Long
    get() = raw.size

  override fun open(): InputStream {
    return raw.open()
  }
}
