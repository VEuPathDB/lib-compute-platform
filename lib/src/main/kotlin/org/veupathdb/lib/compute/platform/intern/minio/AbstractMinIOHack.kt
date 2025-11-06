package org.veupathdb.lib.compute.platform.intern.minio

import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.intern.minio.MinIOHax.sleepCountDuration

@Suppress("NOTHING_TO_INLINE")
internal sealed class AbstractMinIOHack {
  companion object {
    private const val MaxDeleteAttempts = 5
  }

  protected val log = LoggerFactory.getLogger(javaClass)!!

  protected inline fun <T> withRetries(noinline msg: () -> String, noinline fn: () -> T): T =
    context(log) { MinIOHax.withRetries(msg, fn) }

  protected inline fun <T> withRetries(count: Int, noinline msg: () -> String, noinline fn: () -> T): T =
    context(log) { MinIOHax.withRetries(count, msg, fn) }

  protected inline fun sleep() =
    context(log) { MinIOHax.sleep() }

  protected inline fun delete(
    path: String,
    noinline deleteFn: () -> Unit,
    noinline statFn: () -> Boolean,
  ) = context(log) { MinIOHax.delete(path, deleteFn, statFn) }
}