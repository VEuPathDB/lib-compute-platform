@file:JvmName("S3Consts")

package org.veupathdb.lib.compute.platform.s3

/**
 * Name of the `queued` status indication flag object.
 */
const val FlagQueued = ".queued"

/**
 * Name of the `in-progress` status indication flag object.
 */
const val FlagInProgress = ".in-progress"

/**
 * Name of the `complete` status indication flag object.
 */
const val FlagComplete = ".complete"

/**
 * Name of the `failed` status indication flag object.
 */
const val FlagFailed = ".failed"

/**
 * Name of the `expired` status indication flag object.
 */
const val FlagExpired = ".expired"

/**
 * Name of the last-accessed marker object.
 */
const val MarkerLastAccessed = ".last-accessed"