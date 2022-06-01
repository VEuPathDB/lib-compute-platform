package org.veupathdb.lib.compute.platform.intern.db

import com.fasterxml.jackson.databind.JsonNode
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.postgresql.Driver
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.AsyncJob
import org.veupathdb.lib.compute.platform.JobStatus
import org.veupathdb.lib.compute.platform.config.AsyncDBConfig
import org.veupathdb.lib.compute.platform.intern.db.model.JobRecord
import org.veupathdb.lib.compute.platform.intern.db.queries.*
import org.veupathdb.lib.compute.platform.intern.db.queries.GrabJob
import org.veupathdb.lib.compute.platform.intern.db.queries.ListQueuedJobs
import org.veupathdb.lib.compute.platform.intern.db.queries.LookupJob
import org.veupathdb.lib.compute.platform.intern.db.queries.QueueDeadJobs
import org.veupathdb.lib.hash_id.HashID
import java.util.stream.Stream

internal object QueueDB {

  private val Log = LoggerFactory.getLogger(this::class.java)

  private var initialized = false

  internal var ds: HikariDataSource? = null

  @JvmStatic
  internal fun init(config: AsyncDBConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize QueueDB more than once!")

    ds = HikariDataSource(HikariConfig().also {
      it.jdbcUrl         = "jdbc:postgresql://${config.host}:${config.port}/${config.name}"
      it.driverClassName = Driver::class.java.name
      it.username        = config.user
      it.password        = config.pass
      it.maximumPoolSize = config.pool
    })

    initialized = true
  }

  @JvmStatic
  fun grabJob(jobID: HashID) {
    Log.debug("Marking job {} as grabbed", jobID)

    ds!!.connection.use { GrabJob(it, jobID) }
  }

  @JvmStatic
  fun getJob(jobID: HashID) : AsyncJob? {
    Log.debug("Looking up job {}", jobID)

    return ds!!.connection.use {
      val raw = LookupJob(it, jobID)

      if (raw != null) {
        if (raw.status == JobStatus.Queued)
          AsyncDBJob(raw, GetJobQueuePosition(it, jobID))
        else
          AsyncDBJob(raw, null)
      } else {
        null
      }
    }
  }

  @JvmStatic
  fun submitJob(queue: String, jobID: HashID, config: String? = null) {
    Log.debug("Recording new job {} in the database.", jobID)

    ds!!.connection.use { RecordNewJob(it, jobID, queue, config) }
  }

  /**
   * Retrieves a stream of queued job records.
   *
   * The returned stream **MUST** be closed when the caller is done with it to
   * prevent DB connection leaks.
   *
   * @return Stream of queued jobs ordered by job creation date.
   */
  fun getQueuedJobs(): Stream<JobRecord> {
    Log.debug("Getting list of queued jobs.")

    return ds!!.connection.use { ListQueuedJobs(it) }
  }

  /**
   * Retrieves the current database version.
   *
   * If there is no database version set this method returns `null`.
   *
   * @return Current database version or `null` if none is set.
   */
  fun getDatabaseVersion(): String? {
    Log.debug("Getting database version.")

    return ds!!.connection.use { LookupDatabaseVersion(it) }
  }

  /**
   * Changes all `in-progress` jobs in the database to be in the `queued`
   * status.
   */
  fun deadJobCleanup() {
    Log.debug("Executing dead job cleanup.")

    ds!!.connection.use { QueueDeadJobs(it) }
  }
}
