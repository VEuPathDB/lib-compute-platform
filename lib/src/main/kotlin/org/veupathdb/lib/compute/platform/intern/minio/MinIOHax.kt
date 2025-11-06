package org.veupathdb.lib.compute.platform.intern.minio

import org.slf4j.Logger
import org.veupathdb.lib.s3.s34k.errors.S34KError
import org.veupathdb.lib.s3.s34k.objects.S3Object
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal object MinIOHax {
  /**
   * Maximum amount of time that the [delete] method should wait for an object's
   * deletion to be confirmed by MinIO.
   */
  var MaxDeleteAwaitTime = 10.seconds

  /**
   * Amount of time to sleep between MinIO API call retries.
   */
  var RetrySleepTime = 1.seconds

  /**
   * Max number of times an individual MinIO API call should be retried.
   */
  var RetryCount = 3

  /**
   * Executes the given [action], retrying up to [RetryCount] times if the
   * call fails due to an error from MinIO.
   *
   * @param actionMsg Provider for a message describing the action being
   * performed.  This will be called if necessary to write log lines.
   *
   * @param action MinIO interaction function to try to execute.
   *
   * @return The value returned by `action` on successful execution.
   *
   * @throws S34KError If the given [action] fails [RetryCount] times due to
   * MinIO errors.  This exception will contain all MinIO exceptions thrown
   * during executions of the `action` function.
   */
  context(log: Logger)
  fun <T> withRetries(actionMsg: () -> String, action: () -> T): T {
    val errors = ArrayList<S34KError>(RetryCount)
    var msg: String? = null

    for (i in 1..RetryCount) {
      try {
        return action()
      } catch (e: S34KError) {
        if (msg == null)
          msg = actionMsg()

        log.warn("failed {} time(s) to {}", i, msg)
        errors.add(e)
        sleep()
      }
    }

    log.error("failed {} time(s) to {}", RetryCount, msg)
    throw errors.last()
      .apply { errors.forEach { if (it != this) addSuppressed(it) } }
  }

  /**
   * Sleeps for the duration configured by [RetrySleepTime].
   */
  context(log: Logger)
  fun sleep() {
    log.debug("sleeping for {}", { RetrySleepTime })
    Thread.sleep(RetrySleepTime.inWholeMilliseconds)
  }

  /**
   * Convenience method for calling [delete] on an S3Object.
   *
   * @param obj Object to delete from MinIO.
   */
  context(log: Logger)
  fun delete(obj: S3Object) = delete(obj.path, obj::delete, obj::exists)

  /**
   * Tells MinIO to delete an object, then waits for MinIO to confirm the
   * object's deletion, sleeping for [RetrySleepTime] milliseconds between each
   * test for the target object's existence.
   *
   * This method wraps both the given [statFn] and [deleteFn] functions using
   * [withRetries] to retry all interactions with MinIO up to [RetryCount]
   * times.
   *
   * @param path Path of the object to be deleted.  Used for logging.
   *
   * @param deleteFn Function to be called to delete the object from MinIO.
   *
   * @param statFn Function to be called to test if the object still exists in
   * MinIO.
   *
   * @throws S34KError If either of the given functions fail [RetryCount] times
   * due to MinIO errors.  This exception will contain all MinIO exceptions
   * thrown during executions of the failing function.
   */
  context(log: Logger)
  fun delete(path: String, deleteFn: () -> Unit, statFn: () -> Boolean) {
    val startTime = currentSystemMillis()
    var delta = 0.milliseconds

    // Tell minio to delete the object
    withRetries({ "delete object $path" }, deleteFn)

    // Sleep to let minio ponder the delete request
    while (delta < MaxDeleteAwaitTime) {
      sleep()
      delta = currentSystemMillis() - startTime

      if (!withRetries({ "stat object $path" }, statFn))
        return

      log.debug("waiting for object {} deletion: {}", path, delta)
    }


    log.error("waited {} for object {} deletion, but MinIO reports that it still exists", delta, path)

    throw RuntimeException("object deletion timeout for MinIO object $path")
  }

  private fun currentSystemMillis() = System.currentTimeMillis().milliseconds
}
