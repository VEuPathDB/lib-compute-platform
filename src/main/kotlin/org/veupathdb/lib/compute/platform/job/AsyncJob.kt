package org.veupathdb.lib.compute.platform.job

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.hash_id.HashID
import java.time.OffsetDateTime

/**
 * Async Job
 *
 * Represents a job submitted to the compute platform.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
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
   * Will only be present if this process owns the job, and the job is currently
   * in the `queued` status.
   */
  val queuePosition: Int?

  /**
   * Indicates whether this compute platform process owns this job.
   *
   * This field is used to indicate ownership in situations were there are
   * multiple separate compute platform applications sharing a single S3 store
   * instance.
   *
   * If this job was found in the local, managed database, this value will be
   * `true`.
   *
   * If this job was not found in the local, managed database, and was instead
   * found only in the S3 store, this value will be `false`.
   */
  val owned: Boolean

  /**
   * Optional, raw configuration for this job.
   *
   * This will be present if the job was submitted with a configuration.
   *
   * If the job was submitted with no configuration, this value will be `null`.
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