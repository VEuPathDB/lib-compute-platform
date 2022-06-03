package org.veupathdb.lib.compute.platform.intern.db.queries

import org.veupathdb.lib.hash_id.HashID
import java.sql.Connection
import java.time.OffsetDateTime

private const val SQL = """
  SELECT
    job_id
  FROM
    compute.jobs
  WHERE
    status = 'complete'
    AND last_accessed < ?
"""

/**
 * Returns a list of jobs that were last accessed before the given cutoff date.
 *
 * @param con Open connection to be used for the query.
 *
 * @param cutoff Cutoff date used to find prunable jobs.
 *
 * @return List of job IDs for jobs that have expired and are now prunable.
 */
internal fun GetExpiredJobs(con: Connection, cutoff: OffsetDateTime): List<HashID> {
  return con.prepareStatement(SQL).use { ps ->
    ps.setObject(1, cutoff)
    ps.executeQuery().use { rs ->
      val out = ArrayList<HashID>(32)

      while (rs.next()) {
        out.add(HashID(rs.getBytes(1)))
      }

      out
    }
  }
}