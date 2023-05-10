package org.veupathdb.lib.compute.platform.intern.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.postgresql.Driver
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.compute.platform.intern.db.model.JobRecord
import org.veupathdb.lib.compute.platform.intern.db.queries.delete.DeleteJob
import org.veupathdb.lib.compute.platform.intern.db.queries.insert.RecordNewJob
import org.veupathdb.lib.compute.platform.intern.db.queries.select.*
import org.veupathdb.lib.compute.platform.intern.db.queries.update.*
import org.veupathdb.lib.compute.platform.job.AsyncJob
import org.veupathdb.lib.compute.platform.job.JobStatus
import org.veupathdb.lib.hash_id.HashID
import java.time.OffsetDateTime
import java.util.stream.Stream

/**
 * Managed PostgreSQL DB Manager
 *
 * Provides methods for interacting with the async platform library's managed
 * PostgreSQL instance.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
internal object QueueDB {

  private val Log = LoggerFactory.getLogger(this::class.java)

  private var initialized = false

  internal lateinit var ds: HikariDataSource


  /**
   * Initializes this database manager instance.
   */
  @JvmStatic
  internal fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize QueueDB more than once!")

    initialized = true

    Log.info("initializing DB manager")

    ds = HikariDataSource(HikariConfig().also {
      it.jdbcUrl         = "jdbc:postgresql://${config.dbConfig.host}:${config.dbConfig.port}/${config.dbConfig.dbName}"
      it.driverClassName = Driver::class.java.name
      it.username        = config.dbConfig.username
      it.password        = config.dbConfig.password
      it.maximumPoolSize = config.dbConfig.poolSize
    })
  }


  /**
   * Fetches a list of jobs that were last accessed before the given [cutoff]
   * timestamp.
   *
   * @param cutoff Cutoff timestamp.  Jobs that were last accessed before this
   * cutoff will be returned.
   *
   * @return A list of job IDs for jobs that have not been accessed since the
   * given [cutoff] timestamp.
   */
  @JvmStatic
  fun getLastAccessedBefore(cutoff: OffsetDateTime): List<HashID> {
    Log.debug("Fetching list of expired jobs.")
    return ds.connection.use { GetExpiredJobs(it, cutoff) }
  }

  @JvmStatic
  fun markJobAsQueued(jobID: HashID, queue: String) {
    Log.debug("Marking job {} as queued", jobID)
    ds.connection.use { MarkJobQueued(it, jobID, queue) }
  }

  /**
   * Marks the target job as expired in the database.
   *
   * @param jobID Hash ID of the job to be marked as expired.
   *
   * If this job does not exist in the database, no change will be made.
   */
  @JvmStatic
  fun markJobAsExpired(jobID: HashID) {
    Log.debug("Marking job {} as expired", jobID)
    ds.connection.use { MarkJobExpired(it, jobID) }
  }

  /**
   * Marks the target job as failed in the database.
   *
   * Additionally, updates the job record's `finished` timestamp to the current
   * time.
   *
   * @param jobID Hash ID of the job to be marked as failed.
   *
   * If this job does not exist in the database, no change will be made.
   */
  @JvmStatic
  fun markJobAsFailed(jobID: HashID) {
    Log.debug("Marking job {} as failed", jobID)
    ds.connection.use { MarkJobFinished(it, jobID, JobStatus.Failed) }
  }

  @JvmStatic
  fun setJobOutputFiles(jobID: HashID, files: Array<String>) {
    Log.debug("Appending output files to job {}", jobID)
    ds.connection.use { SetJobOutputFiles(it, jobID, files) }
  }

  /**
   * Marks the target job as completed in the database.
   *
   * Additionally, updates the job record's `finished` timestamp to the current
   * time.
   *
   * @param jobID Hash ID of the job to be marked as complete.
   *
   * If this job does not exist in the database, no change will be made.
   */
  @JvmStatic
  fun markJobAsComplete(jobID: HashID) {
    Log.debug("Marking job {} as complete", jobID)
    ds.connection.use { MarkJobFinished(it, jobID, JobStatus.Complete) }
  }


  /**
   * Marks the target job as 'in-progress' in the database.
   *
   * Additionally, updates the job record's `grabbed` timestamp to the current
   * time.
   *
   * @param jobID Hash ID of the job to be marked as in-progress.
   *
   * If the job does not exist in the database, no change will be made.
   */
  @JvmStatic
  fun markJobAsInProgress(jobID: HashID) {
    Log.debug("Marking job {} as in-progress", jobID)
    ds.connection.use { MarkJobInProgress(it, jobID) }
  }


  /**
   * Updates the `last_accessed` field for the target job to the current
   * timestamp.
   *
   * @param jobID Hash ID of the job record to update.
   *
   * If the job does not exist in the database, no change will be made.
   */
  @JvmStatic
  fun updateJobLastAccessed(jobID: HashID) {
    Log.debug("Updating last_modified timestamp for job {}", jobID)
    ds.connection.use { UpdateDBLastAccessed(it, jobID) }
  }


  /**
   * Deletes the job record for the job with the given ID if such a record
   * exists.
   *
   * @param jobID Hash ID of the job to delete.
   *
   * @since 1.2.0
   */
  @JvmStatic
  fun deleteJob(jobID: HashID) {
    Log.debug("Deleting job {}", jobID)
    ds.connection.use { DeleteJob(it, jobID) }
  }

  /**
   * Retrieves the job record for the job with the given ID if such a record
   * exists.
   *
   * @param jobID Hash ID of the job to retrieve.
   *
   * @return The located job record, if it exists, otherwise `null`.
   */
  @JvmStatic
  fun getJob(jobID: HashID) : AsyncDBJob? {
    Log.debug("looking up public job {}", jobID)

    return ds.connection.use {
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
  fun getJobInternal(jobID: HashID): JobRecord? {
    Log.debug("looking up raw job {}", jobID)
    return ds.connection.use { LookupJob(it, jobID) }
  }

  @JvmStatic
  fun submitJob(queue: String, jobID: HashID, config: String?, files: Iterable<String>) {
    Log.debug("Recording new job {} in the database.", jobID)

    ds.connection.use { RecordNewJob(it, jobID, queue, config, files) }
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

    // Connection is not closed here as the caller is responsible for closing
    // the stream.
    return ListQueuedJobs(ds.connection)
  }

  /**
   * Retrieves a stream of queued job records.
   *
   * The returned stream **MUST** be closed when the caller is done with it to
   * prevent DB connection leaks.
   *
   * @return Stream of queued jobs ordered by job creation date.
   */
  fun getRunningJobs(): Stream<JobRecord> {
    Log.debug("Getting list of queued jobs.")

    // Connection is not closed here as the caller is responsible for closing
    // the stream.
    return ListRunningJobs(ds.connection)
  }

  /**
   * Retrieves a stream of all job records.
   *
   * The returned stream **MUST** be closed when the caller is done with it to
   * prevent DB connection leaks.
   *
   * @return Stream of all jobs ordered by job creation date.
   *
   * @since 1.4.0
   */
  fun getAllJobs(): Stream<JobRecord> {
    Log.debug("Getting list of all jobs.")

    // Connection is not closed here as the caller is responsible for closing
    // the stream.
    return ListAllJobs(ds.connection)
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
    return ds.connection.use(::LookupDatabaseVersion)
  }

  /**
   * Changes all `in-progress` jobs in the database to be in the `queued`
   * status.
   */
  fun deadJobCleanup() {
    Log.debug("Executing dead job cleanup.")
    ds.connection.use(::QueueDeadJobs)
  }

  fun metaTableExists(): Boolean {
    Log.debug("Testing if platform meta table exists.")
    return ds.connection.use(::MetaDBTableExists)
  }
}
