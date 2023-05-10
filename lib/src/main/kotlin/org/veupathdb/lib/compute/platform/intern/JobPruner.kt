package org.veupathdb.lib.compute.platform.intern

import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.JobManager
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.s3.S3
import java.time.OffsetDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal object JobPruner : Runnable {

  private val Log = LoggerFactory.getLogger(javaClass)

  private val Exec = Executors.newSingleThreadScheduledExecutor()

  private var expirationDays = 30

  @JvmStatic
  fun init(config: AsyncPlatformConfig) {
    expirationDays = config.jobConfig.expirationDays
  }

  @JvmStatic
  fun schedule() {
    Log.debug("scheduling expired job pruner")
    Exec.scheduleAtFixedRate(this, 0, 12, TimeUnit.HOURS)
  }

  override fun run() {
    Log.info("Starting job pruner")

    val prunableJobs = QueueDB.getLastAccessedBefore(OffsetDateTime.now().minusDays(expirationDays.toLong()))

    Log.info("Found {} expired jobs", prunableJobs.size)

    prunableJobs.forEach { JobManager.setJobExpired(it) }

    Log.info("Job pruning complete")
  }
}