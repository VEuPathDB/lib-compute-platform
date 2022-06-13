package org.veupathdb.lib.compute.platform.intern.ws

import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.hash_id.HashID
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

/**
 * Local Scratch Workspace Manager
 *
 * Provides methods for working with local scratch workspaces.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
internal object ScratchSpaces {

  private val Log = LoggerFactory.getLogger(javaClass)

  private var initialized: Boolean = false

  private var wsRoot: Path? = null

  fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize ScratchSpaces more than once!")

    Log.debug("initializing scratch workspace manager")

    initialized = true

    wsRoot = Path.of(config.localWorkspaceRoot)

    if (!wsRoot!!.exists())
      wsRoot!!.createDirectories()
  }

  @JvmStatic
  fun create(jobID: HashID): LocalWorkspace {
    Log.debug("creating local scratch workspace for job {}", jobID)
    return LocalWorkspace(wsRoot!!.resolve(jobID.string).createDirectory())
  }
}