package org.veupathdb.lib.compute.platform.intern.minio

import org.slf4j.Logger
import org.veupathdb.lib.s3.s34k.errors.S34KError
import org.veupathdb.lib.s3.s34k.objects.S3Object
import kotlin.time.Duration.Companion.milliseconds

@Suppress("NOTHING_TO_INLINE")
internal object MinIOHax {
  var MaxDeleteAttempts = 5

  var SleepMillis = 1_000L

  var RetryCount = 3

  fun sleepCountDuration(sleepCount: Int) = (sleepCount * SleepMillis).milliseconds

  context(log: Logger)
  fun <T> withRetries(actionMsg: () -> String, fn: () -> T) =
    withRetries(RetryCount, actionMsg, fn)

  context(log: Logger)
  fun <T> withRetries(retries: Int, actionMsg: () -> String, fn: () -> T): T {
    var lastError: S34KError? = null

    for (i in 1..retries) {
      try {
        return fn()
      } catch (e: S34KError) {
        log.warn("failed {} time(s) to {}", i, actionMsg())

        if (lastError != null)
          e.addSuppressed(lastError)

        lastError = e

        sleep()
      }
    }

    log.error("failed {} time(s) to {}", RetryCount, actionMsg())
    throw lastError!!
  }

  context(log: Logger)
  fun sleep() {
    log.debug("sleeping for {}", { SleepMillis.milliseconds })
    Thread.sleep(SleepMillis)
  }

  context(log: Logger)
  fun delete(obj: S3Object) = delete(obj.path, obj::delete, obj::exists)

  context(log: Logger)
  fun delete(path: String, deleteFn: () -> Unit, statFn: () -> Boolean) {
    // Tell minio to delete the object
    deleteFn()

    // Sleep for a bit to let minio ponder the delete request
    sleep()

    // If minio is being particularly dim, give it some more time to work it out
    for (i in 1..MaxDeleteAttempts) {
      if (!statFn())
        return

      log.debug("waiting for object {} deletion: {}", path, sleepCountDuration(i))
      sleep()
    }

    log.error("waited {} for object {} deletion, but MinIO reports that it still exists", sleepCountDuration(MaxDeleteAttempts), path)
    throw RuntimeException("object deletion timeout for MinIO object $path")
  }
}
