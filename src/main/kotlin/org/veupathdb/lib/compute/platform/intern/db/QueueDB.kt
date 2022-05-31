package org.veupathdb.lib.compute.platform.intern.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.postgresql.Driver
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncDBConfig
import org.veupathdb.lib.compute.platform.intern.db.queries.GrabJob
import org.veupathdb.lib.hash_id.HashID

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
  operator fun contains(jobID: HashID) = hasJob(jobID)

  @JvmStatic
  operator fun get(jobID: HashID) = getJob(jobID)


  fun deadJobCleanup() {}

  fun getQueuePosition(jobID: HashID): Int {}

  fun getDatabaseVersion(): String? {}



}