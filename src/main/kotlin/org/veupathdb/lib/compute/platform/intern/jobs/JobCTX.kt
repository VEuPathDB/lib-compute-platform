package org.veupathdb.lib.compute.platform.intern.jobs

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.JobContext
import org.veupathdb.lib.compute.platform.JobWorkspace
import org.veupathdb.lib.hash_id.HashID

data class JobCTX(
  override val jobID: HashID,
  override val config: JsonNode?,
  override val workspace: JobWorkspace
) : JobContext