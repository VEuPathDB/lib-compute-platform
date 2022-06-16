package org.veupathdb.lib.compute.platform.intern.db.queries.select

import org.veupathdb.lib.compute.platform.intern.db.model.JobRecord
import org.veupathdb.lib.compute.platform.job.JobStatus
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.jackson.Json
import java.sql.ResultSet
import java.time.OffsetDateTime

internal fun ResultSet.toJobRow() =
  JobRecord(
    HashID(getBytes(1)),                         // job_id
    JobStatus.fromString(getString(2)),          // status
    getString(3),                                // queue
    getString(4)?.let(Json::parse),              // config
    getArray(5).toStringArray(),                 // input_files
    getObject(6, OffsetDateTime::class.java),    // created
    getObject(7, OffsetDateTime::class.java),    // last_accessed
    getObject(8, OffsetDateTime::class.java),    // grabbed
    getObject(9, OffsetDateTime::class.java),    // finished
  )


@Suppress("UNCHECKED_CAST")
private fun java.sql.Array.toStringArray() = array as Array<String>