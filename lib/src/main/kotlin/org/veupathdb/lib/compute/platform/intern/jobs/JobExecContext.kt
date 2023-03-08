package org.veupathdb.lib.compute.platform.intern.jobs

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.job.JobExecutorContext
import org.veupathdb.lib.hash_id.HashID

data class JobExecContext(
  override val queue: String,
  override val jobID: HashID,
  override val jobConfig: JsonNode?
) : JobExecutorContext
