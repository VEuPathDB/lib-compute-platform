package org.veupathdb.lib.compute.platform.intern.db

import org.veupathdb.lib.compute.platform.intern.db.model.JobRecord
import org.veupathdb.lib.compute.platform.job.AsyncJob

internal class AsyncDBJob(
  private val raw: JobRecord,
  override val queuePosition: Int?
) : AsyncJob {
  override val jobID
    get() = raw.jobID

  override val status
    get() = raw.status

  override val owned = true

  override val config
    get() = raw.config

  override val created
    get() = raw.created

  override val grabbed
    get() = raw.grabbed

  override val finished
    get() = raw.finished
}