package org.veupathdb.lib.compute.platform.intern.jobs

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.intern.FileConfig
import org.veupathdb.lib.compute.platform.intern.db.QueueDB
import org.veupathdb.lib.compute.platform.intern.s3.S3
import org.veupathdb.lib.compute.platform.intern.ws.ScratchSpaces
import org.veupathdb.lib.compute.platform.job.JobExecutor
import org.veupathdb.lib.compute.platform.job.JobFileReference
import org.veupathdb.lib.compute.platform.job.JobResultStatus
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
  fun execute(jobID: HashID, conf: JsonNode?): JobResultStatus {

    Log.debug("executing job {}", jobID)

    ScratchSpaces.create(jobID).use { workspace ->
      // Write the input config to file (if it exists)
      conf?.also { workspace.write(FileConfig, it) }

      // Lookup the job in the DB to get the input file list.
      val dbJob = QueueDB.getJobInternal(jobID)

      // If the job wasn't found something has gone terribly wrong
      if (dbJob == null) {
        Log.error("db job lookup failed in job execution for job {}", jobID)
        return JobResultStatus.Failure
      }

      // If the job does have input files
      if (dbJob.includedFiles.isNotEmpty()) {

        // fetch the list of available files from the S3 workspace
        val s3Files = S3.getNonReservedFiles(jobID)

        // If the list of available files in the S3 workspace is empty, we
        // goofed somewhere along the line and we can't execute this job.
        if (s3Files.isEmpty()) {
          Log.error("db job specifies input files, but none were found in S3 workspace for job {}", jobID)
          return JobResultStatus.Failure
        }

        // index the S3 files
        val index = HashMap<String, JobFileReference>(dbJob.includedFiles.size)
        s3Files.forEach { index[it.name] = it }

        // Ensure we have all the required files in S3
        if (!validateIndex(jobID, dbJob.includedFiles, index))
          return JobResultStatus.Failure

        // Download all the files from S3 into the local workspace.
        try {
          dbJob.includedFiles.forEach { index[it]!!.open().use { s -> workspace.write(it, s) } }
        } catch (e: Throwable) {
          Log.error("failed to download files from s3 to local scratch workspace for job $jobID", e)
          return JobResultStatus.Failure
        }
      }


      // Execute the job via the given JobExecutor implementation.
      val res = executor.execute(JobCTX(jobID, conf, workspace))

      // Persist the outputs of the job to S3.
      S3.persistFiles(jobID, workspace.getFiles(res.outputFiles))

      // Return the job's status
      return res.status
    }
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