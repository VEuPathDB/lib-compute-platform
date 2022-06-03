package org.veupathdb.lib.compute.platform.intern.db.queries

import java.sql.Connection

private const val SQL = """
  SELECT
    value
  FROM
    compute.meta
  WHERE
    key = 'version'
"""

/**
 * Looks up the currently recorded database migration version.
 *
 * @param con Open database connection to use for the query.
 *
 * @return The current database version, or `null` if no database migrations
 * have yet taken place.
 */
fun LookupDatabaseVersion(con: Connection) =
  con.createStatement().use { stmt ->
    stmt.executeQuery(SQL).use { rs ->
      if (rs.next())
        rs.getString(1)
      else
        null
    }
  }