package org.veupathdb.lib.compute.platform.intern.minio

import org.veupathdb.lib.s3.workspaces.java.WorkspaceFile
import java.io.File

internal class FuglyMinIOWorkspaceFile(private val delegate: WorkspaceFile): AbstractMinIOHack(),
WorkspaceFile by delegate {
  override fun size() =
    withRetries({ "stat object $absolutePath" }, delegate::size)

  override fun open() =
    withRetries({ "open object $absolutePath" }, delegate::open)

  override fun download(localFile: File) =
    withRetries({ "download object $absolutePath to file ${localFile.path}" }) { delegate.download(localFile) }

  override fun delete() =
    delete(absolutePath, delegate::delete, ::stat)

  // workaround to check for object existence.  If the object doesn't exist, the
  // underlying method throws an NPE when attempting to access the file size
  // property.
  private fun stat() =
    try {
      size()
      true
    } catch (_: NullPointerException) {
      false
    }
}