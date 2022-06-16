package org.veupathdb.lib.compute.platform.job

/**
 * Async Job Status
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
enum class JobStatus {
  Queued,
  InProgress,
  Complete,
  Failed,
  Expired,
  ;

  /**
   * Indicates whether this status is a "finished" status.
   *
   * This will only be true if this enum value is [Complete] or [Failed].
   */
  val isFinished
    get() = this == Failed || this == Complete

  override fun toString() =
    when (this) {
      Queued     -> "queued"
      InProgress -> "in-progress"
      Complete   -> "complete"
      Failed     -> "failed"
      Expired    -> "expired"
    }

  companion object {
    @JvmStatic
    fun fromString(value: String) =
      when (value) {
        "queued"      -> Queued
        "in-progress" -> InProgress
        "complete"    -> Complete
        "failed"      -> Failed
        "expired"     -> Expired
        else          -> throw IllegalArgumentException("Unrecognized JobStatus value: $value")
      }
  }
}