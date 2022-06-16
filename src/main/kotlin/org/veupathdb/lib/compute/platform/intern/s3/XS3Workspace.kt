package org.veupathdb.lib.compute.platform.intern.s3

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.intern.*
import org.veupathdb.lib.compute.platform.job.JobStatus
import org.veupathdb.lib.jackson.Json
import org.veupathdb.lib.s3.s34k.errors.S34KError
import org.veupathdb.lib.s3.workspaces.S3Workspace

/**
 * Extended S3 Workspace
 *
 * Extends the [S3Workspace] type with additional methods and fields specific to
 * the async compute platform.
 */
internal class XS3Workspace(private val raw: S3Workspace) : S3Workspace by raw {

  /**
   * The timestamp for when this job was queued from the S3 store.
   */
  @get:Throws(S34KError::class, IllegalStateException::class)
  val queuedDate by lazy {
    (raw[FlagQueued] ?: throw IllegalStateException("Workspace ${raw.id} has no .queued flag."))
      .lastModified!!
  }

  /**
   * The timestamp for when this job was marked as in progress from the S3
   * store.
   *
   * If this job was never marked as in-progress, this value will be `null`.
   */
  @get:Throws(S34KError::class)
  val grabbedDate by lazy { raw[FlagInProgress]?.lastModified }

  /**
   * The timestamp for when this job was marked completed from the S3 store.
   *
   * If this job was never marked as completed, this value will be `null`.
   */
  @get:Throws(S34KError::class)
  val completedDate by lazy { raw[FlagComplete]?.lastModified }

  /**
   * The timestamp for when this job was marked failed from the S3 store.
   *
   * If this job was never marked as failed, this value will be `null`.
   */
  @get:Throws(S34KError::class)
  val failedDate by lazy { raw[FlagFailed]?.lastModified }

  /**
   * The timestamp for when this job was marked as either completed or failed
   * from the S3 store.
   *
   * If this job was never marked as completed or failed, this value will be
   * `null`.
   */
  @get:Throws(S34KError::class)
  val finishedDate by lazy { completedDate ?: failedDate }

  /**
   * The timestamp for when this job was marked as expired in the S3 store.
   *
   * If this job was never marked as expired, this value will be `null`.
   *
   * Note: Failed jobs are never marked as expired.
   */
  @get:Throws(S34KError::class)
  val expiredDate by lazy { raw[FlagExpired]?.lastModified }

  /**
   * Determines the status of the job represented by this workspace by testing
   * what status flags exist in the workspace.
   *
   * If the `failed` flag exists, this method returns [JobStatus.Failed].
   *
   * If the `expired` flag exists, this method returns [JobStatus.Expired].
   *
   * If the `complete` flag exists, this method returns [JobStatus.Complete].
   *
   * If the `in-progress` flag exists, this method returns
   * [JobStatus.InProgress].
   *
   * If none of the above flags exist, this method returns [JobStatus.Queued].
   *
   * @return The status of the job represented by this workspace, derived from
   * the contents of the workspace.
   */
  fun deriveStatus(): JobStatus {
    return when {
      failedDate != null    -> JobStatus.Failed
      expiredDate != null   -> JobStatus.Expired
      completedDate != null -> JobStatus.Complete
      grabbedDate != null   -> JobStatus.InProgress
      else                  -> JobStatus.Queued
    }
  }

  /**
   * Loads and returns the raw configuration for the job represented by this
   * workspace.
   *
   * If this workspace has no config file saved, this method returns `null`.
   *
   * @return The raw JSON config for this job, or `null` if the workspace does
   * not contain a config file.
   */
  @Throws(S34KError::class)
  fun getConfig() =
    if (FileConfig in raw) {
      raw.open(FileConfig).use { Json.parse<JsonNode>(it) }
    } else {
      null
    }
}