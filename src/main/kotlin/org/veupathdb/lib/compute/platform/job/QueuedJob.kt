package org.veupathdb.lib.compute.platform.job

import org.veupathdb.lib.hash_id.HashID
import java.time.OffsetDateTime

interface QueuedJob {
  val jobID: HashID

  val queued: OffsetDateTime

  val rawConfig: String
}