package org.veupathdb.lib.compute.platform

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.jobs.JobExecutors
import org.veupathdb.lib.compute.platform.intern.queues.JobQueues
import org.veupathdb.lib.hash_id.HashID

object AsyncPlatform {

  private var initialized = false

  @JvmStatic
  fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize AsyncPlatform more than once!")

    JobQueues.init(config)
    JobExecutors.init(config)
    QueueDB.init(config)

    initialized = true
  }

  @JvmStatic
  @JvmOverloads
  fun submitJob(queue: String, jobID: HashID, rawConfig: JsonNode? = null) {
    JobQueues.submitJob(queue, jobID, rawConfig)
  }

  @JvmStatic
  fun getJob(jobID: HashID): AsyncJob? {
    // Check db for job
    //   if exists return job
    // Check s3 for job
    //   if exists return job
    // return null
  }
}