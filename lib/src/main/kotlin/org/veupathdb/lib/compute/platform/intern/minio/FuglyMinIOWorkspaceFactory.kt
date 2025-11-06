package org.veupathdb.lib.compute.platform.intern.minio

import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.s3.workspaces.java.S3WorkspaceFactory

/**
 * Now with more hacks!!
 */
internal class FuglyMinIOWorkspaceFactory(private val delegate: S3WorkspaceFactory): AbstractMinIOHack() {

  fun get(jobID: HashID) =
    withRetries({ "open workspace for job $jobID" }) { delegate.get(jobID) }
      ?.let(::FuglyMinIOWorkspace)

  fun create(jobID: HashID) =
    FuglyMinIOWorkspace(withRetries({ "create workspace for job $jobID" }) { delegate.create(jobID) })
}