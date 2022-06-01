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

fun LookupDatabaseVersion(con: Connection) =
  con.createStatement().use { stmt ->
    stmt.executeQuery(SQL).use { rs ->
      if (rs.next())
        rs.getString(1)
      else
        null
    }
  }