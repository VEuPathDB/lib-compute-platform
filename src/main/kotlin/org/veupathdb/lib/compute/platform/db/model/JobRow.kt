package org.veupathdb.lib.compute.platform.db.model

import org.veupathdb.lib.compute.platform.JobStatus
import org.veupathdb.lib.hash_id.HashID
import java.time.OffsetDateTime

data class JobRow(
  val jobID:    HashID,
  val status:   JobStatus,
  val config:   String,
  val created:  OffsetDateTime,
  val grabbed:  OffsetDateTime?,
  val finished: OffsetDateTime?,
  val extra:    Map<String, String>
)
