package org.veupathdb.lib.compute.platform.intern.minio

import org.veupathdb.lib.s3.workspaces.java.SubS3Workspace

internal class FuglyMinIOSubWorkspace(delegate: SubS3Workspace, override val parent: FuglyMinIOWorkspace)
  : FuglyMinIOWorkspace(delegate)
  , SubS3Workspace