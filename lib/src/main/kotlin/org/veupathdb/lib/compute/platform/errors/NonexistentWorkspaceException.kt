package org.veupathdb.lib.compute.platform.errors

import org.veupathdb.lib.hash_id.HashID

class NonexistentWorkspaceException : IllegalStateException {

  val jobID: HashID

  constructor(jobID: HashID) : super(makeMessage(jobID)) {
    this.jobID = jobID
  }

  constructor(jobID: HashID, message: String) : super(message) {
    this.jobID = jobID
  }

  constructor(jobID: HashID, cause: Throwable) : super(makeMessage(jobID), cause) {
    this.jobID = jobID
  }

  constructor(jobID: HashID, message: String, cause: Throwable) : super(message, cause) {
    this.jobID = jobID
  }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun makeMessage(jobID: HashID) =
  "expected workspace to exist for job $jobID, but it didn't"