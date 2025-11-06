package org.veupathdb.lib.compute.platform.intern.minio

import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.s3.workspaces.java.S3Workspace
import java.io.File

internal open class FuglyMinIOWorkspace(private val delegate: S3Workspace)
  : AbstractMinIOHack()
  , S3Workspace by delegate
{
  override fun exists() =
    withRetries({ "test for workspace $path" }, delegate::exists)

  override fun open(path: String) =
    withRetries({ "open object ${objectPath(path)}" }) { delegate.open(path) }

  override fun get(path: String) =
    withRetries({ "get object ${objectPath(path)}" }) { delegate[path] }
      ?.let(::FuglyMinIOWorkspaceFile)

  override fun copy(from: String, to: File) =
    withRetries({ "copy object ${objectPath(path)} to file ${to.path}" }) { delegate.copy(from, to) }

  override fun files() =
    withRetries({ "list files in workspace $path" }, delegate::files)
      .map(::FuglyMinIOWorkspaceFile)

  override fun contains(path: String) =
    withRetries({ "stat object ${objectPath(path)}" }) { path in delegate }

  override fun delete() =
    delete(path, delegate::delete, ::exists)

  override fun hasSubWorkspace(id: HashID) =
    withRetries({ "test for sub-workspace ${objectPath(id.string)}" }) { delegate.hasSubWorkspace(id) }

  override fun openSubWorkspace(id: HashID) =
    withRetries({ "open sub-workspace ${objectPath(id.string)}" }) { delegate.openSubWorkspace(id) }
      ?.let { FuglyMinIOSubWorkspace(it, this@FuglyMinIOWorkspace) }

  override fun createSubWorkspace(id: HashID) =
    FuglyMinIOSubWorkspace(
      withRetries({ "create sub-workspace ${objectPath(id.string)}" }) { delegate.createSubWorkspace(id) },
      this@FuglyMinIOWorkspace,
    )

  @Suppress("NOTHING_TO_INLINE")
  private inline fun objectPath(name: String) = "$path/$name"
}