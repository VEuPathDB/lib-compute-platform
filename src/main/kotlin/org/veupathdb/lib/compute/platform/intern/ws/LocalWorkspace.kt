package org.veupathdb.lib.compute.platform.intern.ws

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.JobWorkspace
import org.veupathdb.lib.jackson.Json
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.Reader
import java.nio.file.Path
import kotlin.io.path.*

internal class LocalWorkspace(override val path: Path) : JobWorkspace {

  override fun touch(path: String) =
    resolveForWriting(path)

  override fun delete(path: String) {
    resolve(path).deleteIfExists()
  }

  override fun mkdir(path: String) =
    resolve(path).createDirectories()

  override fun write(path: String, data: InputStream) =
    resolveForWriting(path).also { it.outputStream().use { os -> data.transferTo(os); os.flush() } }

  override fun write(path: String, data: Reader) =
    resolveForWriting(path).also { it.writer().use { ow -> data.transferTo(ow); ow.flush() } }

  override fun write(path: String, data: String) =
    resolveForWriting(path).also { it.writer().use { ow -> ow.write(data); ow.flush() } }

  override fun write(path: String, data: JsonNode) =
    resolveForWriting(path).also { it.writer().use { ow -> ow.write(data.toString()); ow.flush() } }

  override fun copy(fromPath: String, toPath: String) =
    resolveAndRequire(fromPath).let { from -> resolveMkDirs(toPath).also { to -> from.copyTo(to) } }

  override fun openStream(path: String) =
    resolveAndRequire(path).inputStream()

  override fun openReader(path: String) =
    resolveAndRequire(path).reader()

  override fun readAsString(path: String) =
    resolveAndRequire(path).readText()

  override fun readAsJson(path: String) =
    Json.parse<JsonNode>(resolveAndRequire(path).inputStream())

  fun delete() {
    path.toFile().deleteRecursively()
  }

  fun getFiles(files: List<String>): List<Path> {
    val out = ArrayList<Path>(files.size)

    files.forEach {
      val path = resolve(it)
      if (path.exists())
        out.add(path)
    }

    return out
  }


  @Suppress("NOTHING_TO_INLINE")
  private inline fun resolve(path: String): Path =
    if (path.startsWith('/'))
      throw IllegalArgumentException("Paths must be relative!")
    else
      this.path.resolve(path)

  @Suppress("NOTHING_TO_INLINE")
  private inline fun resolveAndRequire(path: String): Path =
    resolve(path).also {
      if (!it.exists())
        throw FileNotFoundException("File $it not found!")
    }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun resolveForWriting(path: String): Path =
    resolveMkDirs(path).also {
      it.deleteIfExists()
      it.createFile()
    }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun resolveMkDirs(path: String): Path =
    resolve(path).also { it.parent.createDirectories() }
}