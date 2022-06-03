package org.veupathdb.lib.compute.platform.intern.db.model

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.JobStatus
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.jackson.Json
import java.sql.ResultSet
import java.time.OffsetDateTime

/**
 * Database Job Record
 *
 * Represents a single job in the Queue Database.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 *
 * @constructor Creates a new [JobRecord] instance.
 *
 * @param jobID Hash ID of the job this record represents.
 *
 * @param status Status of the job as of the time this record was fetched.
 *
 * @param queue Name/ID of the queue this job was submitted to.
 *
 * @param config Raw, serialized configuration for this job.
 *
 * @param created Timestamp of when this job was created.
 *
 * @param grabbed Timestamp of when this job was started.
 *
 * This will be `null` if the job is still queued.
 *
 * @param finished Timestamp of when this job finished (successfully or not).
 *
 * This will be `null` if the job has not yet finished.
 */
internal data class JobRecord (
  val jobID:        HashID,
  val status:       JobStatus,
  val queue:        String,
  val config:       JsonNode?,
  val created:      OffsetDateTime,
  val lastAccessed: OffsetDateTime,
  val grabbed:      OffsetDateTime?,
  val finished:     OffsetDateTime?,
)

