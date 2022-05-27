package org.veupathdb.lib.compute.platform.internal.s3

import org.slf4j.LoggerFactory
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.s3.s34k.buckets.S3Bucket
import java.time.OffsetDateTime

/**
 * # S3 Workspace Cache Pruner
 *
 * Utility that locates workspace that have not been accessed since before now
 * minus the given number of [timeoutDays] and deletes them, replacing them with
 * an expired job flag.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 *
 * @constructor Constructs a new [CachePruner] instance.
 *
 * @param s3 S3 bucket wrapper.
 *
 * @param timeoutDays Number of days before today that a job must have been
 * accessed within to be kept.  All jobs that have not been accessed within this
 * window will be considered 'expired' and deleted.
 */
class CachePruner(
  private val s3: S3Bucket,
  private val timeoutDays: Int
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Prunes the S3 workspace cache.
   *
   * Locates all workspaces that have not been accessed since before
   * [timeoutDays] ago and deletes them, replacing the contents with an
   * 'expired' flag.
   *
   * @return A list of the job IDs of the workspaces that were deleted.
   */
  fun pruneCache(): List<HashID> {
    log.info("Beginning cache pruning in bucket {}", s3.name)

    // Prepend the marker file name with a slash to ensure we don't catch any
    // weird files that happen to have a name ending with ".last-accessed".
    val objName = "/$MarkerLastAccessed"

    // Get the cutoff date.  Last accessed flags created before this cutoff will
    // be targeted for deletion.
    val cutoff = OffsetDateTime.now().minusDays(timeoutDays.toLong())

    log.debug("Finding workspaces to prune.")

    val expired = s3.objects.listAll()
      // Stream over all object entries in the S3 bucket.
      .stream()
      // Filter out entries that do not end with the flag object name
      .filter { it.path.endsWith(objName) }
      // Filter out entries that are not older than our cutoff
      // Since this is a 'get' operation on S3, the lastModified value should be
      // non-null.
      .filter { it.lastModified!!.isBefore(cutoff) }
      // Get the "directory" name from the path
      .map { it.dirName }
      // Collect the list of directories we need to delete
      .toList()

    log.info("Located {} expired S3 workspaces", expired.size)

    // For each workspace entry:
    expired.forEach {
      // Try to delete the workspace 'directory'
      try {
        log.debug("Attempting to delete workspace {}", it)
        s3.objects.rmdir(it)
      } catch (e: Throwable) {
        log.error("Failed to delete workspace $it", e)
      }

      // Try to write an expired flag
      try {
        log.debug("Attempting to write expired flag for workspace {}", it)
        s3.objects.touch("$it/$FlagExpired")
      } catch (e: Throwable) {
        log.error("Failed to write expired flag for workspace $it", e)
      }
    }

    log.info("Finished cache pruning in bucket {}", s3.name)

    // Get the list of job IDs from the workspace paths.
    return expired.stream()
      .map { it.substring(it.lastIndexOf('/')+1) }
      .map { HashID(it) }
      .toList()
  }
}