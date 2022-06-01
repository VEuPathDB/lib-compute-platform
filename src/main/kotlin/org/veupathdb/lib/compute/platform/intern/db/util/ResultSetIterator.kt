package org.veupathdb.lib.compute.platform.intern.db.util

import org.slf4j.LoggerFactory
import java.sql.ResultSet

class ResultSetIterator(private var rs: ResultSet) : Iterator<ResultSet> {

  private val Log = LoggerFactory.getLogger(ResultSetIterator::class.java)

  override fun hasNext(): Boolean {
    Log.debug("Indexing DB cursor by 1 row")
    return rs.next()
  }

  override fun next(): ResultSet {
    Log.debug("Returning handle on ResultSet")
    return rs
  }
}