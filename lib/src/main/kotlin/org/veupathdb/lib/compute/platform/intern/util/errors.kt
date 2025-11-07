package org.veupathdb.lib.compute.platform.intern.util

internal inline fun <T> errorToNull(fn: () -> T): T? =
  try { fn() }
  catch (_: Throwable) { null }