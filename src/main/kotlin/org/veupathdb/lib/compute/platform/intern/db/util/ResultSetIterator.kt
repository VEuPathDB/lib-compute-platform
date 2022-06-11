package org.veupathdb.lib.compute.platform.intern.db.util

import java.sql.ResultSet

internal class ResultSetIterator(private var rs: ResultSet) : Iterator<ResultSet> {

  override fun hasNext(): Boolean {
    return rs.next()
  }

  override fun next(): ResultSet {
    return rs
  }
}