package org.veupathdb.lib.compute.platform.intern.db

import org.slf4j.LoggerFactory
import java.util.zip.ZipInputStream

private const val MigrationPath = "db/migrations/"

internal class DatabaseMigrator : Runnable {

  private val Log = LoggerFactory.getLogger(this::class.java)

  private val thisJar
    get() = this::class.java.protectionDomain.codeSource.location

  override fun run() {
    Log.info("Checking for database updates.")

    val version = if (QueueDB.metaTableExists())
      QueueDB.getDatabaseVersion() ?: "0.0.0"
    else
      "0.0.0"

    Log.debug("Starting from version {}", version)

    val migs = listMigrations(version)

    Log.debug("Found {} migrations", migs.size)

    if (migs.isNotEmpty()) {
      QueueDB.ds!!.connection.use { con ->
        migs.forEach {
          Log.info("Executing database migration script: {}", it)
          con.createStatement().use { stmt -> stmt.execute(loadSQL(it)) }
        }
      }
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

  /**
   * Parse the migration version number out of the path string.
   *
   * Expected input: `db/migrations/1.0.0/000.sql`
   *
   * Expected output: `1.0.0`
   *
   * @param path Path from which the migration version should be parsed.
   *
   * @return Migration version.
   */
  @Suppress("NOTHING_TO_INLINE")
  private inline fun migVersion(path: String) =
    path.substring(MigrationPath.length, path.lastIndexOf('/'))

  /**
   * Reads the SQL contents out of a given resource path.
   *
   * @param path Path to the SQL resource to read.
   *
   * @return The contents of the SQL resource.
   */
  @Suppress("NOTHING_TO_INLINE")
  private inline fun loadSQL(path: String) =
    String(this::class.java.getResourceAsStream("/$path")!!.readAllBytes())
}