package org.veupathdb.lib.compute.platform.intern.s3

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.AsyncJob
import org.veupathdb.lib.compute.platform.config.AsyncS3Config
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.s3.s34k.S3Api
import org.veupathdb.lib.s3.s34k.S3Client
import org.veupathdb.lib.s3.s34k.S3Config
import org.veupathdb.lib.s3.workspaces.S3WorkspaceFactory

internal object QueueS3 {

  private val Log = LoggerFactory.getLogger(javaClass)

  private var initialized = false

  private var s3: S3Client? = null

  private var wsf: S3WorkspaceFactory? = null

  @JvmStatic
  fun init(conf: AsyncS3Config) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize QueueS3 more than once!")

    initialized = true

    s3 = S3Api.newClient(S3Config(
      conf.host,
      conf.port.toUShort(),
      conf.https,
      conf.access,
      conf.secret
    ))

    wsf = S3WorkspaceFactory(s3!!, conf.bucket, conf.root)

  }

  @JvmStatic
  fun getJob(jobID: HashID): AsyncJob? {
    wsf!![jobID]?.let(::XS3Workspace)
  }

  @JvmStatic
  fun deleteJob(jobID: HashID) {
    Log.debug("Deleting workspace for job {} in S3", jobID)
    wsf!![jobID]?.delete()
  }

  @JvmStatic
  fun expireJob(jobID: HashID) {
    Log.debug("Expiring workspace for job {} in S3", jobID)

    // Load the workspace
    val ws = wsf!![jobID] ?: throw IllegalStateException("Attempted to expire nonexistent workspace $jobID")

    // Iterate through the files in the workspace
    ws.files().forEach {
      // If the target file does not start with a `.` character
      if (!it.startsWith('.')) {
        // delete it
        ws.delete(it)
      }
    }

    // Create the expired flag.
    ws.touch(FlagExpired)
  }

  @JvmStatic
  fun submitJob(jobID: HashID, conf: JsonNode? = null) {
    Log.debug("Creating workspace for job {} in S3", jobID)

    val ws = wsf!!.create(jobID)

    ws.touch(FlagQueued)

    if (conf != null)
      ws.write(FileConfig, conf.toString().byteInputStream())
  }
}