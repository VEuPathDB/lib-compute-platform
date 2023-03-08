package org.veupathdb.lib.compute.platform.intern.s3

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncS3Config
import org.veupathdb.lib.compute.platform.intern.*
import org.veupathdb.lib.compute.platform.job.AsyncJob
import org.veupathdb.lib.compute.platform.job.JobFileReference
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.s3.s34k.S3Api
import org.veupathdb.lib.s3.s34k.S3Client
import org.veupathdb.lib.s3.s34k.S3Config
import org.veupathdb.lib.s3.s34k.fields.BucketName
import org.veupathdb.lib.s3.workspaces.S3Workspace
import org.veupathdb.lib.s3.workspaces.S3WorkspaceFactory
import java.io.File
import java.io.InputStream
import java.nio.file.Path

/**
 * S3 Manager
 *
 * Provides methods for interacting with the S3 store backing the async compute
 * platform library.
 *
 * This layer provides methods only for specific, targeted operations, and not
 * general access to S3.  This is to keep the operations used by this platform
 * organized and accounted for.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
internal object S3 {

  private val Log = LoggerFactory.getLogger(javaClass)

  private var initialized = false

  private lateinit var s3: S3Client

  private lateinit var config: AsyncS3Config

  private lateinit var wsf: S3WorkspaceFactory

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
      conf.accessToken,
      conf.secretKey
    ))

    wsf = S3WorkspaceFactory(s3, conf.bucket, conf.rootPath)

    config = conf
  }


  /**
   * Attempts to fetch the target job from the S3 store.
   *
   * @param jobID Hash ID of the workspace/job to fetch.
   *
   * @return The target job, if a workspace for it exists, otherwise `null`.
   */
  @JvmStatic
  fun getJob(jobID: HashID): AsyncJob? {
    Log.debug("Fetching workspace {} from S3", jobID)
    return wsf[jobID]?.let(::XS3Workspace)?.let(::AsyncS3Job)
  }


  /**
   * Attempts to persist the list of files (and/or directories) to the S3 store.
   *
   * @param jobID Hash ID of the workspace into which the given files should be
   * persisted.
   *
   * @param files List of files to persist.
   */
  fun persistFiles(jobID: HashID, files: List<Path>) {
    Log.debug("persisting {} files to workspace {} in S3", files.size, jobID)

    val ws = wsf[jobID] ?: throw IllegalStateException("Attempted to write result files to nonexistent workspace $jobID")

    files.forEach {
      persistFile(ws, it.toFile())
    }
  }

  private fun persistFile(ws: S3Workspace, src: File, prefix: String = "") {
    if (src.isDirectory) {
      val pfx = "$prefix${src.name}/"
      src.listFiles()!!.forEach { persistFile(ws, it, pfx) }
    } else {
      val name = "$prefix${src.name}"
      Log.debug("writing file {} as {} to workspace {} in S3", src, name, ws.id)
      ws.copy(src, name)
    }
  }

  /**
   * Deletes the workspace for the target job.
   *
   * @param jobID Hash ID of the job workspace that should be deleted.
   *
   * @throws IllegalStateException If the target job does not exist.
   *
   * @since 1.2.0
   */
  @JvmOverloads
  fun deleteWorkspace(jobID: HashID, throwOnNotExists: Boolean = true) {
    val ws = wsf[jobID]

    if (ws == null && throwOnNotExists) {
      throw IllegalStateException("Attempted to delete nonexistent workspace $jobID")
    }

    ws?.delete()
  }

  /**
   * Fetches the input and output files from the target workspace.
   *
   * @param jobID Hash ID of the workspace from which the files should be
   * retrieved.
   *
   * @return The list of files present in the workspace.
   *
   * This list will not include any flag files or the input config file.
   */
  @JvmStatic
  fun getNonReservedFiles(jobID: HashID): List<JobFileReference> {
    Log.debug("Fetching result files from workspace {} in S3", jobID)

    // Load the workspace
    val ws = wsf[jobID] ?: throw IllegalStateException("Attempted to lookup result files from nonexistent workspace $jobID")

    // Fetch the list of files from the workspace
    val files = ws.files()

    // Instantiate our output list
    val out = ArrayList<JobFileReference>(files.size)

    // Iterate through the files in the S3 workspace
    files.forEach {
      // If the file is not a flag file and is not the input config
      if (!IsReservedFilename(it.name)) {
        // Add it to the output list
        out.add(JobFileReferenceImpl(it))
      }
    }

    return out
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
    val ws = wsf[jobID] ?: throw IllegalStateException("Attempted to expire nonexistent workspace $jobID")

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

    val ws = wsf[jobID] ?: throw IllegalStateException("Attempted to reset nonexistent workspace $jobID")

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
   *
   * @param inputs Input files that will be uploaded to the S3 workspace.
   */
  @JvmStatic
  fun submitWorkspace(jobID: HashID, conf: JsonNode?, inputs: Map<String, () -> InputStream>) {
    Log.debug("creating workspace for job {} in s3", jobID)

    val ws = wsf.create(jobID)

    Log.debug("writing queued flag to s3 workspace {}", jobID)
    ws.touch(FlagQueued)

    if (conf != null) {
      Log.debug("writing file {} to s3 workspace {}", FileConfig, jobID)
      conf.toString().byteInputStream().use { ws.write(FileConfig, it) }
    }

    inputs.forEach { (k, v) ->
      Log.debug("writing file {} to s3 workspace {}", k, jobID)
      v().use { ws.write(k, it) }
    }
  }

  fun markWorkspaceAsInProgress(jobID: HashID) {
    Log.debug("marking workspace for job {} as in-progress in s3", jobID)

    val ws = wsf[jobID] ?: throw IllegalStateException("Attempted to mark nonexistent workspace $jobID as in-progress")
    ws.touch(FlagInProgress)
  }

  fun markWorkspaceAsFailed(jobID: HashID) {
    Log.debug("marking workspace for job {} as failed in s3", jobID)

    val ws = wsf[jobID] ?: throw IllegalStateException("Attempted to mark nonexistent workspace $jobID as failed")
    ws.touch(FlagFailed)
  }

  fun markWorkspaceAsComplete(jobID: HashID) {
    Log.debug("marking workspace for job {} as failed in s3", jobID)

    val ws = wsf[jobID] ?: throw IllegalStateException("Attempted to mark nonexistent workspace $jobID as complete")
    ws.touch(FlagComplete)
  }

  /**
   * Lists the IDs of all the jobs under the root path in S3.
   *
   * @since 1.4.0
   */
  fun listJobIDs(): Sequence<HashID> {
    return s3.buckets[BucketName(config.bucket)]!!
      .objects
      .listSubPaths(config.rootPath)
      .commonPrefixes()
      .asSequence()
      .map { try { HashID(it) } catch (e: Throwable) { null } }
      .filterNotNull()
  }
}