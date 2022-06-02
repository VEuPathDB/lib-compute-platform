package org.veupathdb.lib.compute.platform.intern.s3

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.AsyncJob
import org.veupathdb.lib.compute.platform.JobResultReference
import org.veupathdb.lib.compute.platform.config.AsyncS3Config
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.s3.s34k.S3Api
import org.veupathdb.lib.s3.s34k.S3Client
import org.veupathdb.lib.s3.s34k.S3Config
import org.veupathdb.lib.s3.workspaces.S3WorkspaceFactory

internal object S3 {

  private val Log = LoggerFactory.getLogger(javaClass)

  private var initialized = false

  private var s3: S3Client? = null

  private var wsf: S3WorkspaceFactory? = null

  @JvmStatic
  fun init(conf: AsyncS3Config) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize QueueS3 more than once!")

    initialized = true

    Log.debug("initializing S3 manager")

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
  fun getResultFiles(jobID: HashID): List<JobResultReference> {
    Log.debug("Fetching result files from workspace {} in S3", jobID)

    // Load the workspace
    val ws = wsf!![jobID] ?: throw IllegalStateException("Attempted to lookup result files from nonexistent workspace $jobID")

    // Fetch the list of files from the workspace
    val files = ws.files()

    // Instantiate our output list
    val out = ArrayList<JobResultReference>(files.size)

    // Iterate through the files in the S3 workspace
    files.forEach {
      // If the file is not a flag file and is not the input config
      if (!IsFlagFilename(it.name) && it.name != FileConfig) {
        // Add it to the output list
        out.add(JobResultReferenceImpl(it))
      }
    }

    return out
  }

  @JvmStatic
  fun deleteWorkspace(jobID: HashID) {
    Log.debug("Deleting workspace for job {} in S3", jobID)
    wsf!![jobID]?.delete()
  }


  /**
   * Clears out the target workspace and marks it as `expired`.
   *
   * All files apart from the flag files and config file will be deleted from
   * the workspace and an empty [FlagExpired] flag will be written.
   *
   * @param jobID Hash ID of the workspace to mark as expired.
   *
   * @throws IllegalStateException If the target workspace does not exist in S3.
   */
  @JvmStatic
  fun expireWorkspace(jobID: HashID) {
    Log.debug("Expiring workspace for job {} in S3", jobID)

    // Load the workspace
    val ws = wsf!![jobID] ?: throw IllegalStateException("Attempted to expire nonexistent workspace $jobID")

    // Iterate through the files in the workspace
    ws.files().forEach {
      // If the target file is not a flag file
      if (!IsFlagFilename(it.name)) {
        // delete it
        Log.debug("Deleting S3 object {}", it.absolutePath)
        it.delete()
      }
    }

    // Create the expired flag.
    ws.touch(FlagExpired)
  }


  /**
   * Returns a job workspace back to its initial `queued` state (the state it
   * was in just after creation).
   *
   * @param jobID ID of the workspace to reset/requeue.
   *
   * @throws IllegalStateException If the target workspace does not exist in S3.
   */
  @JvmStatic
  fun resetWorkspace(jobID: HashID) {
    Log.debug("Resetting workspace for job {} in S3", jobID)

    val ws = wsf!![jobID] ?: throw IllegalStateException("Attempted to reset nonexistent workspace $jobID")

    // Iterate through the files in the workspace
    ws.files().forEach {
      // If the target file is not the input config and is not the queued flag
      if (it.name != FileConfig && it.name != FlagQueued) {
        // delete it
        Log.debug("Deleting S3 object {}", it.absolutePath)
        it.delete()
      }
    }
  }

  /**
   * Submits a new job workspace to the S3 store.
   *
   * The workspace will be created with an empty [FlagQueued] file marking the
   * job as queued, and the given config (if present) will be written to
   * [FileConfig] in the workspace.
   *
   * **NOTE**: This method makes no attempt to test whether the desired
   * workspace already exists.  That check should be performed before calling
   * this method.
   *
   * @param jobID ID of the workspace to create.
   *
   * @param conf Optional JSON configuration to write to the workspace.
   */
  @JvmStatic
  fun submitWorkspace(jobID: HashID, conf: JsonNode? = null) {
    Log.debug("Creating workspace for job {} in S3", jobID)

    val ws = wsf!!.create(jobID)

    ws.touch(FlagQueued)

    if (conf != null)
      ws.write(FileConfig, conf.toString().byteInputStream())
  }
}