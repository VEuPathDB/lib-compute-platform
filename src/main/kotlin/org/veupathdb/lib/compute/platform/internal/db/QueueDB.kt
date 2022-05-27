package org.veupathdb.lib.compute.platform.internal.db

import com.zaxxer.hikari.HikariDataSource
import org.veupathdb.lib.compute.platform.conf.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.internal.db.model.JobRecord
import org.veupathdb.lib.hash_id.HashID

object QueueDB {

  private var initialized = false

  internal var ds: HikariDataSource? = null

  @JvmStatic
  internal fun init(config: AsyncPlatformConfig) {

  }

  @JvmStatic
  fun hasJob(jobID: HashID): Boolean {}

  @JvmStatic
  operator fun contains(jobID: HashID) = hasJob(jobID)

  @JvmStatic
  fun getJob(jobID: HashID): JobRecord? {}

  @JvmStatic
  operator fun get(jobID: HashID) = getJob(jobID)


  fun deadJobCleanup() {}

  fun getQueuePosition(jobID: HashID): Int {}

  fun getDatabaseVersion(): String? {}



}