package org.veupathdb.lib.compute.platform.internal.db

import org.slf4j.LoggerFactory
import java.util.zip.ZipInputStream

private const val MigrationPath = "db/migrations/"

internal class DatabaseMigrator : Runnable {

  private val Log = LoggerFactory.getLogger(this::class.java)

  private val thisJar
    get() = this::class.java.protectionDomain.codeSource.location

  override fun run() {
    Log.info("Checking for database updates.")

    val version = QueueDB.getDatabaseVersion() ?: "0.0.0"

    Log.debug("Starting from version {}", version)

    val migs = listMigrations(version)

    Log.debug("Found {} migrations", migs.size)

    if (migs.isNotEmpty()) {
      QueueDB.ds
    }


  }

  private fun listMigrations(version: String): List<String> {
    val jarStream  = ZipInputStream(thisJar.openStream())
    val migrations = ArrayList<String>(10)

    while (true) {
      val entry = (jarStream.nextEntry ?: break).name

      if (!entry.startsWith(MigrationPath) || !entry.endsWith(".sql"))
        continue

      if (migVersion(entry) <= version)
        continue

      migrations.add(entry)
    }

    migrations.sort()

    return migrations
  }

  private fun migVersion(path: String) = path.substring(MigrationPath.length, path.length-4)


}