package org.veupathdb.lib.compute.platform.intern.ws

import org.veupathdb.lib.compute.platform.config.AsyncPlatformConfig
import org.veupathdb.lib.hash_id.HashID
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

internal object ScratchSpaces {

  private var initialized: Boolean = false

  private var wsRoot: Path? = null

  fun init(config: AsyncPlatformConfig) {
    if (initialized)
      throw IllegalStateException("Attempted to initialize ScratchSpaces more than once!")

    initialized = true

    wsRoot = Path.of(config.localWorkspaceRoot)

    if (!wsRoot!!.exists())
      wsRoot!!.createDirectories()
  }

  @JvmStatic
  fun create(jobID: HashID) =
    LocalWorkspace(wsRoot!!.resolve(jobID.string).createDirectory())
}