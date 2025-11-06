package org.veupathdb.lib.compute.platform.intern.minio

import org.slf4j.LoggerFactory

@Suppress("NOTHING_TO_INLINE")
internal sealed class AbstractMinIOHack {
  protected val log = LoggerFactory.getLogger(javaClass)!!

  protected inline fun <T> withRetries(noinline msg: () -> String, noinline fn: () -> T): T =
    context(log) { MinIOHax.withRetries(msg, fn) }

  protected inline fun delete(
    path: String,
    noinline deleteFn: () -> Unit,
    noinline statFn: () -> Boolean,
  ) = context(log) { MinIOHax.delete(path, deleteFn, statFn) }
}