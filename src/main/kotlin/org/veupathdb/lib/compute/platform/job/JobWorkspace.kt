package org.veupathdb.lib.compute.platform.job

import com.fasterxml.jackson.databind.JsonNode
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.nio.file.Path
import java.util.function.Consumer

/**
 * Local Scratch Workspace
 *
 * Represents a handle on a local workspace and provides methods for operating
 * on files and directories in that workspace.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
interface JobWorkspace {

  /**
   * Absolute path to this workspace on the local filesystem.
   */
  val path: Path

  /**
   * Creates an empty file in this workspace at the given path.
   *
   * @param path Relative path at which the empty file will be created.
   *
   * If this path includes subdirectories, those subdirectories will be created
   * if they do not already exist.
   *
   * @return Path to the created file.
   */
  fun touch(path: String): Path

  /**
   * Deletes the file at the given path if such a file exists.
   *
   * If the target file does not exist, this method does nothing.
   *
   * @param path Relative path to the file that should be deleted.
   */
  fun delete(path: String)

  /**
   * Creates an empty directory at the given path.
   *
   * @param path Relative path at which the directory will be created.
   *
   * If this path includes subdirectories, those subdirectories will be created
   * if they do not already exist.
   *
   * @return Path to the created directory.
   */
  fun mkdir(path: String): Path

  /**
   * Writes the contents of the given stream to the file at the given path.
   *
   * If the given path points to a file that already exists, it will be
   * truncated before writing.
   *
   * If the given path points to a file that does not yet exist, it will be
   * created.
   *
   * @param path Relative path at which the file should be written.
   *
   * If this path includes subdirectories, those subdirectories will be created
   * if they do not already exist.
   *
   * @param data Input stream over the data that will be written to the target
   * file.
   *
   * @return Path to the written file.
   */
  fun write(path: String, data: InputStream): Path

  /**
   * Opens an output stream to the given path and passes it to the consumer,
   * which can write arbitrary data to the file, then closes the stream.
   *
   * If the given path points to a file that already exists, it will be
   * truncated before writing.
   *
   * If the given path points to a file that does not yet exist, it will be
   * created.
   *
   * @param path Relative path at which the file should be written.
   *
   * If this path includes subdirectories, those subdirectories will be created
   * if they do not already exist.
   *
   * @param consumer Consumer of the created output stream which will write
   * data to the target file via the stream.
   *
   * @return Path to the written file.
   */
  fun write(path: String, consumer: Consumer<OutputStream>): Path

  /**
   * Writes the contents of the given reader to the file at the given path.
   *
   * If the given path points to a file that already exists, it will be
   * truncated before writing.
   *
   * If the given path points to a file that does not yet exist, it will be
   * created.
   *
   * @param path Relative path at which the file should be written.
   *
   * If this path includes subdirectories, those subdirectories will be created
   * if they do not already exist.
   *
   * @param data Reader over the data that will be written to the target file.
   *
   * @return Path to the written file.
   */
  fun write(path: String, data: Reader): Path

  /**
   * Writes the given string to the file at the given path.
   *
   * If the given path points to a file that already exists, it will be
   * truncated before writing.
   *
   * If the given path points to a file that does not yet exist, it will be
   * created.
   *
   * @param path Relative path at which the file should be written.
   *
   * If this path includes subdirectories, those subdirectories will be created
   * if they do not already exist.
   *
   * @param data String that will be written to the target file.
   *
   * @return Path to the written file.
   */
  fun write(path: String, data: String): Path


  /**
   * Writes the given [JsonNode] to the file at the given path.
   *
   * If the given path points to a file that already exists, it will be
   * truncated before writing.
   *
   * If the given path points to a file that does not yet exist, it will be
   * created.
   *
   * @param path Relative path at which the file should be written.
   *
   * If this path includes subdirectories, those subdirectories will be created
   * if they do not already exist.
   *
   * @param data Json data that will be written to the target file.
   *
   * @return Path to the written file.
   */
  fun write(path: String, data: JsonNode): Path

  /**
   * Copies the file at the given [fromPath] to the given [toPath].
   *
   * @param fromPath Relative path to the file that will be copied.
   *
   * @param toPath Relative path to where the file should be copied.
   *
   * If this path includes subdirectories, those subdirectories will be created
   * if they do not already exist.
   *
   * @return Path to the new file copy.
   *
   * @throws FileNotFoundException if the file at [fromPath] does not exist.
   */
  fun copy(fromPath: String, toPath: String): Path

  /**
   * Opens an [InputStream] over the contents of the file at the given [path].
   *
   * @param path Relative path to the file that will be opened.
   *
   * @return An [InputStream] over the contents of the target file.
   *
   * @throws FileNotFoundException If the file at [path] does not exist.
   */
  fun openStream(path: String): InputStream

  /**
   * Opens a [Reader] over the contents of the file at the given [path].
   *
   * @param path Relative path to the file that will be opened.
   *
   * @return A [Reader] over the contents of the target file.
   *
   * @throws FileNotFoundException If the file at [path] does not exist.
   */
  fun openReader(path: String): Reader

  /**
   * Reads the contents of the file at the given [path] into a [String] and
   * returns it.
   *
   * @param path Relative path to the file that will be read.
   *
   * @return The [String] contents of the target file.
   *
   * @throws FileNotFoundException If the file at [path] does not exist.
   */
  fun readAsString(path: String): String

  /**
   * Reads the contents of the file at the given [path] as a JSON value and
   * returns it.
   *
   * @param path Relative path to the file that will be read.
   *
   * @return The parsed [JsonNode] contents of the target file.
   *
   * @throws FileNotFoundException If the file at [path] does not exist.
   */
  fun readAsJson(path: String): JsonNode
}
