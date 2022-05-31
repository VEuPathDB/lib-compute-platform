package org.veupathdb.lib.compute.platform.intern.s3

import org.veupathdb.lib.compute.platform.AsyncJob
import org.veupathdb.lib.compute.platform.config.AsyncS3Config
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.s3.s34k.S3Api
import org.veupathdb.lib.s3.s34k.S3Client
import org.veupathdb.lib.s3.s34k.S3Config
import org.veupathdb.lib.s3.workspaces.S3WorkspaceFactory

internal object QueueS3 {

  private var initialized = false

  private var s3: S3Client? = null

  private var ws: S3WorkspaceFactory? = null

  @JvmStatic
  fun init(conf: AsyncS3Config) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize QueueS3 more than once!")

    s3 = S3Api.newClient(S3Config(
      conf.host,
      conf.port.toUShort(),
      conf.https,
      conf.access,
      conf.secret
    ))

    ws = S3WorkspaceFactory(s3!!, conf.bucket, conf.root)

    initialized = true
  }

  @JvmStatic
  fun getJob(jobID: HashID): AsyncJob? {
    ws!![jobID]?.let(::XS3Workspace)
  }
}