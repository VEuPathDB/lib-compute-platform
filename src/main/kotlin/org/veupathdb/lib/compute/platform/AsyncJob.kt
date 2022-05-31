package org.veupathdb.lib.compute.platform

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.hash_id.HashID
import java.time.OffsetDateTime

interface AsyncJob {
  /**
   * Hash ID of this job.
   */
  val jobID: HashID

  /**
   * Status of this job.
   */
  val status: JobStatus

  /**
   * Position of this job in the queue.
   *
   * Will only be present if this process owns the job and the job is currently
   * in the `queued` status.
   */
  val queuePosition: Int?

  /**
   * Indicates whether this process owns this job.
   */
  val owned: Boolean

  /**
   * Raw configuration for this job.
   */
  val config: JsonNode?

  /**
   * Timestamp for when this job was created (initially queued).
   */
  val created: OffsetDateTime

  /**
   * Timestamp for when this job was pulled off the queue and started.
   *
   * If this job is still queued, this value will be `null`.
   */
  val grabbed: OffsetDateTime?

  /**
   * Timestamp for when this job was completed (successfully or not).
   *
   * If this job
   */
  val finished: OffsetDateTime?
}