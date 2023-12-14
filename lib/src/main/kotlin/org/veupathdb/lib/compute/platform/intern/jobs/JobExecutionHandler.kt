package org.veupathdb.lib.compute.platform.intern.jobs

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.JobManager
import org.veupathdb.lib.compute.platform.intern.FileConfig
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.s3.S3
import org.veupathdb.lib.compute.platform.intern.ws.ScratchSpaces
import org.veupathdb.lib.compute.platform.job.*
import org.veupathdb.lib.hash_id.HashID

/**
 * Job executor with context.
 *
 * Executes the given job with the configured [JobExecutor] implementation,
 * providing the execution context to the [JobExecutor.execute] method.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
internal class JobExecutionHandler(private val executor: JobExecutor) {

  private val Log = LoggerFactory.getLogger(javaClass)

  /**
   * Executes the given job on the configured [JobExecutor].
   *
   * Provides the execution with a local scratch workspace and the input files
   * needed to execute the job.
   *
   * At this point in the process, the S3 workspace should only be populated
   * with the `queued`, `in-progress`, `input-config`, and the job's input
   * files.
   */
  fun execute(jobID: HashID, conf: JsonNode?): PlatformJobResultStatus {

    Log.debug("executing job {}", jobID)

    ScratchSpaces.create(jobID).use { workspace ->
      // Write the input config to file (if it exists)
      conf?.also { workspace.write(FileConfig, it) }

      // Lookup the job in the DB to get the input file list.
      val dbJob = QueueDB.getJobInternal(jobID)

      // If the job wasn't found the job was most likely deleted.
      if (dbJob == null) {
        Log.error("job {} was deleted and is being aborted", jobID)
        return PlatformJobResultStatus.Aborted
      }

      // If the job does have input files
      if (dbJob.includedFiles.isNotEmpty()) {

        // fetch the list of available files from the S3 workspace
        val s3Files = S3.getNonReservedFiles(jobID)

        // If the list of available files in the S3 workspace is empty, we
        // goofed somewhere along the line and we can't execute this job.
        if (s3Files.isEmpty()) {
          Log.error("db job specifies input files, but none were found in S3 workspace for job {}", jobID)
          return PlatformJobResultStatus.Failure
        }

        // index the S3 files
        val index = HashMap<String, JobFileReference>(dbJob.includedFiles.size)
        s3Files.forEach { index[it.name] = it }

        // Ensure we have all the required files in S3
        if (!validateIndex(jobID, dbJob.includedFiles, index))
          return PlatformJobResultStatus.Failure

        // Download all the files from S3 into the local workspace.
        try {
          dbJob.includedFiles.forEach { index[it]!!.open().use { s -> workspace.write(it, s) } }
        } catch (e: Throwable) {
          Log.error("failed to download files from s3 to local scratch workspace for job $jobID", e)
          return PlatformJobResultStatus.Failure
        }
      }

      // Verify that the job is still valid (hasn't been deleted or expired
      // while it was waiting in the queue).
      if (!jobIsStillRunnable(jobID)) {
        Log.info("Aborting job {} before job execution for no longer being in a runnable state (deleted or expired)", jobID)
        return PlatformJobResultStatus.Aborted
      }

      // Execute the job via the given JobExecutor implementation.
      val res = executor.execute(JobContext(jobID, conf, workspace))

      // Verify that the job is _still_ still valid (didn't get deleted or
      // expired out from under us while we were running the executor).
      if (!jobIsStillRunnable(jobID)) {
        Log.info("Aborting job {} after job execution for no longer being in a runnable state (deleted or expired)", jobID)
        return PlatformJobResultStatus.Aborted
      }

      // Persist the outputs of the job to S3.
      S3.persistFiles(jobID, workspace.getFiles(res.outputFiles))

      // Record the output files on the job row
      QueueDB.setJobOutputFiles(jobID, res.outputFiles.toTypedArray())

      // Verify that the job wasn't invalidated while we were busy writing files
      // to S3.
      if (jobWasInvalidated(jobID)) {
        Log.info("Aborting job {} for being invalidated.", jobID)
        S3.wipeWorkspace(jobID)
        return PlatformJobResultStatus.Aborted
      }

      // Return the job's status
      return res.status.toPlatformStatus()
    }
  }

  private fun jobIsStillRunnable(jobID: HashID): Boolean {
    val (dbJob, s3Job) = JobManager.getJob(jobID)

    if (dbJob == null) {
      Log.debug("While checking whether job {} is still runnable the, job could not be located in the queue database; returning false", jobID)
      return false
    }

    if (s3Job == null) {
      Log.debug("While checking whether job {} is still runnable, the job could not be located in S3; returning false", jobID)
      return false
    }

    return if (dbJob.status == JobStatus.Expired) {
      Log.debug("While checking whether job {} is still runnable, the job status in the queue db was found to be expired; returning false", jobID)
      false
    } else if (s3Job.status == JobStatus.Expired) {
      Log.debug("While checking whether job {} is still runnable, the job status in S3 was found to be expired; returning false", jobID)
      false
    } else {
      Log.debug("Job {} was found to still be runnable", jobID)
      true
    }
  }

  private fun jobWasInvalidated(jobID: HashID): Boolean {
    val (dbJob, s3Job) = JobManager.getJob(jobID)

    dbJob ?: return true
    s3Job ?: return true

    return false
  }

  private fun validateIndex(jobID: HashID, files: Array<String>, index: Map<String, *>): Boolean {
    var valid = true

    files.forEach {
      if (it !in index) {
        Log.error("required input file {} is missing from the S3 workspace for job {}", it, jobID)
        valid = false
      }
    }

    return valid
  }
}