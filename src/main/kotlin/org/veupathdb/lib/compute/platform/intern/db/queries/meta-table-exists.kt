package org.veupathdb.lib.compute.platform.intern.db.queries

import java.sql.Connection

private const val SQL = """
  SELECT
    count(1)
  FROM
    information_schema.tables
  WHERE
    table_schema = 'compute'
    AND table_type = 'BASE_TABLE'
    AND table_name = 'meta'
"""

internal fun MetaDBTableExists(con: Connection): Boolean {
  con.createStatement().use { stmt ->
    stmt.executeQuery(SQL).use { rs ->
      rs.next()
      return rs.getInt(1) > 0
    }
  }
}