package org.veupathdb.lib.compute.platform.errors

/**
 * Exception thrown when attempting an illegal operation due to a job not yet
 * being in a "finished" state (complete or failed).
 */
class IncompleteJobException : IllegalStateException {
  constructor() : super()

  constructor(message: String) : super(message)

  constructor(cause: Throwable) : super(cause)

  constructor(message: String, cause: Throwable) : super(message, cause)
}