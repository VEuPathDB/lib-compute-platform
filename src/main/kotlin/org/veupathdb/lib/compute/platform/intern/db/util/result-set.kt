package org.veupathdb.lib.compute.platform.intern.db.util

import java.sql.ResultSet
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Returns a [Stream] over the rows in a [ResultSet].
 *
 * The returned [Stream] must be closed to avoid connection leaks.
 */
internal fun ResultSet.stream(): Stream<ResultSet> {
  val out = StreamSupport.stream(
    Spliterators.spliteratorUnknownSize(
      ResultSetIterator(this),
      Spliterator.IMMUTABLE or Spliterator.ORDERED
    ),
    false
  )

  out.onClose {
    this.statement.connection.close()
    this.statement.close()
    this.close()
  }

  return out
}