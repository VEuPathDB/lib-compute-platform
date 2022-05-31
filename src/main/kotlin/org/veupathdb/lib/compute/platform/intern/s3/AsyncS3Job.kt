package org.veupathdb.lib.compute.platform.intern.s3

import org.veupathdb.lib.compute.platform.AsyncJob
import org.veupathdb.lib.hash_id.HashID

class AsyncS3Job(private val raw: XS3Workspace) : AsyncJob {
  override val jobID: HashID
    get() = raw.id

  override val status by lazy { raw.deriveStatus() }

  override val queuePosition = -1

  override val owned = false

  override val config by lazy { raw.getConfig() }

  override val created
    get() = raw.queuedDate

  override val grabbed
    get() = raw.grabbedDate

  override val finished
    get() = raw.finishedDate
}