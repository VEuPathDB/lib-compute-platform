@file:JvmName("S3Consts")

package org.veupathdb.lib.compute.platform.intern.s3

/**
 * Name of the `queued` status indication flag object.
 */
internal const val FlagQueued = ".queued"

/**
 * Name of the `in-progress` status indication flag object.
 */
internal const val FlagInProgress = ".in-progress"

/**
 * Name of the `complete` status indication flag object.
 */
internal const val FlagComplete = ".complete"

/**
 * Name of the `failed` status indication flag object.
 */
internal const val FlagFailed = ".failed"

/**
 * Name of the `expired` status indication flag object.
 */
internal const val FlagExpired = ".expired"

internal const val FileConfig = "input-config"

internal fun IsFlagFilename(name: String): Boolean {
  return when(name) {
    FlagQueued     -> true
    FlagInProgress -> true
    FlagComplete   -> true
    FlagFailed     -> true
    FlagExpired    -> true
    else           -> false
  }
}