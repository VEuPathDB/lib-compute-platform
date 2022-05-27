package org.veupathdb.lib.compute.platform.db.model

import org.veupathdb.lib.compute.platform.JobStatus
import org.veupathdb.lib.hash_id.HashID
import java.io.InputStream
import java.io.Reader
import java.time.OffsetDateTime

/**
 * @param config Configuration value.
 * If this parameter value is an [InputStream], its contents will be written to
 * the config field in the database.
 *
 * If this parameter value is a [Reader], its contents will be written to the
 * config field in the database.
 *
 * If this parameter value is a [String], its contents will be written to the
 * config field in the database.
 *
 * If this parameter is any other type, its [toString] value will be written to
 * the config field in the database.
 */
data class NewJob(
  val jobID:   HashID,
  val config:  Any,
  val status:  JobStatus      = JobStatus.Queued,
  val created: OffsetDateTime = OffsetDateTime.now()
) {
  val extra: MutableMap<String, String> = HashMap()
}
