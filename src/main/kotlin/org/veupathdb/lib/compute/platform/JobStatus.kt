package org.veupathdb.lib.compute.platform

enum class JobStatus {
  Queued,
  InProgress,
  Complete,
  Failed,
  Expired,
  ;

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