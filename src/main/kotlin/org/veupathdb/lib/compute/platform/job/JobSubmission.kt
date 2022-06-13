package org.veupathdb.lib.compute.platform.job

import com.fasterxml.jackson.databind.JsonNode
import org.veupathdb.lib.compute.platform.intern.IsReservedFilename
import org.veupathdb.lib.compute.platform.intern.util.ReaderInputStream
import org.veupathdb.lib.hash_id.HashID
import java.io.File
import java.io.InputStream
import java.io.Reader

class JobSubmission private constructor(
  internal val jobID: HashID,
  internal val config: JsonNode?,
  internal val inputs: Map<String, () -> InputStream>
) {

  companion object {
    @JvmStatic
    fun builder() = Builder()

    @JvmStatic
    inline fun build(fn: Builder.() -> Unit) = Builder().also(fn).build()
  }

  class Builder {

    var jobID: HashID? = null
      set(value) {
        if (value == null)
          throw IllegalArgumentException("Job ID cannot be null")

        field = value
      }

    var config: JsonNode? = null

    private val inputs = HashMap<String, () -> InputStream>(10)

    fun jobID(jobID: HashID): Builder {
      this.jobID = jobID
      return this
    }

    fun config(config: JsonNode): Builder {
      this.config = config
      return this
    }

    /**
     * Adds the given file to the job submission inputs.
     *
     * Job submission inputs are persisted to the S3 store and will be made
     * available to the job in the local scratch workspace on execution.
     *
     * The name of the input file will be kept and used as the filename in both
     * S3 and the local scratch space.
     *
     * The input file name must not be one of the reserved file names listed in
     * the project documentation.
     *
     * @param file File to add as an input to this job.
     *
     * @return This [Builder] instance.
     *
     * @throws IllegalArgumentException If the name of the given file is one of
     * the reserved file names.
     */
    fun addInputFile(file: File): Builder {
      if (IsReservedFilename(file.name))
        throw IllegalArgumentException("Cannot set an input file with the reserved file name ${file.name}")

      this.inputs[file.name] = file::inputStream
      return this
    }

    /**
     * Adds the given file to the job submission inputs.
     *
     * Job submission inputs are persisted to the S3 store and will be made
     * available to the job in the local scratch workspace on execution.
     *
     * The given [name] will be used as the filename in both S3 and the local
     * scratch space.
     *
     * The given [name] must not be one of the reserved file names listed in the
     * project documentation.
     *
     * @param name Name to use for the file in S3 and the local scratch space.
     *
     * Must not be a reserved file name.
     *
     * @param file File to add as an input to this job.
     *
     * @return This [Builder] instance.
     *
     * @throws IllegalArgumentException If the given [name] is one of the
     * reserved file names.
     */
    fun addInputFile(name: String, file: File): Builder {
      if (IsReservedFilename(name))
        throw IllegalArgumentException("Cannot set an input file with the reserved file name $name")

      this.inputs[name] = file::inputStream
      return this
    }

    /**
     * Adds the given content as a file to the job submission inputs.
     *
     * Job submission inputs are persisted to the S3 store and will be made
     * available to the job in the local scratch workspace on execution.
     *
     * The given [name] will be used as the filename in both S3 and the local
     * scratch space.
     *
     * The given [name] must not be one of the reserved file names listed in the
     * project documentation.
     *
     * @param name Name to use for the file in S3 and the local scratch space.
     *
     * Must not be a reserved file name.
     *
     * @param content Content to be written to file in S3 and the local scratch
     * space.
     *
     * @return This [Builder] instance.
     *
     * @throws IllegalArgumentException If the given [name] is one of the
     * reserved file names.
     */
    fun addInputFile(name: String, content: String): Builder {
      if (IsReservedFilename(name))
        throw IllegalArgumentException("Cannot set an input file with the reserved file name $name")

      this.inputs[name] = { content.byteInputStream() }
      return this
    }

    /**
     * Adds the given content as a file to the job submission inputs.
     *
     * Job submission inputs are persisted to the S3 store and will be made
     * available to the job in the local scratch workspace on execution.
     *
     * The given [name] will be used as the filename in both S3 and the local
     * scratch space.
     *
     * The given [name] must not be one of the reserved file names listed in the
     * project documentation.
     *
     * @param name Name to use for the file in S3 and the local scratch space.
     *
     * Must not be a reserved file name.
     *
     * @param content Content to be written to file in S3 and the local scratch
     * space.
     *
     * @return This [Builder] instance.
     *
     * @throws IllegalArgumentException If the given [name] is one of the
     * reserved file names.
     */
    fun addInputFile(name: String, content: InputStream): Builder {
      if (IsReservedFilename(name))
        throw IllegalArgumentException("Cannot set an input file with the reserved file name $name")

      this.inputs[name] = { content }
      return this
    }

    /**
     * Adds the given content as a file to the job submission inputs.
     *
     * Job submission inputs are persisted to the S3 store and will be made
     * available to the job in the local scratch workspace on execution.
     *
     * The given [name] will be used as the filename in both S3 and the local
     * scratch space.
     *
     * The given [name] must not be one of the reserved file names listed in the
     * project documentation.
     *
     * @param name Name to use for the file in S3 and the local scratch space.
     *
     * Must not be a reserved file name.
     *
     * @param content Content to be written to file in S3 and the local scratch
     * space.
     *
     * @return This [Builder] instance.
     *
     * @throws IllegalArgumentException If the given [name] is one of the
     * reserved file names.
     */
    fun addInputFile(name: String, content: JsonNode): Builder {
      if (IsReservedFilename(name))
        throw IllegalArgumentException("Cannot set an input file with the reserved file name $name")

      this.inputs[name] = { content.toString().byteInputStream() }
      return this
    }

    /**
     * Adds the given content as a file to the job submission inputs.
     *
     * Job submission inputs are persisted to the S3 store and will be made
     * available to the job in the local scratch workspace on execution.
     *
     * The given [name] will be used as the filename in both S3 and the local
     * scratch space.
     *
     * The given [name] must not be one of the reserved file names listed in the
     * project documentation.
     *
     * @param name Name to use for the file in S3 and the local scratch space.
     *
     * Must not be a reserved file name.
     *
     * @param content Content to be written to file in S3 and the local scratch
     * space.
     *
     * @return This [Builder] instance.
     *
     * @throws IllegalArgumentException If the given [name] is one of the
     * reserved file names.
     */
    fun addInputFile(name: String, content: Reader): Builder {
      if (IsReservedFilename(name))
        throw IllegalArgumentException("Cannot set an input file with the reserved file name $name")

      this.inputs[name] = { ReaderInputStream(content) }
      return this
    }

    /**
     * Validates the configuration set on this builder and constructs a new
     * [JobSubmission] instance.
     *
     * @throws IllegalStateException If any of the following are true:
     *
     * * [jobID] is `null`
     * * Any of the names of the configured input files are reserved.
     */
    fun build(): JobSubmission {
      if (jobID == null)
        throw IllegalStateException("Cannot construct a JobSubmission instance with a null job id")

      inputs.keys.forEach {
        if (IsReservedFilename(it)) {
          throw IllegalStateException("Cannot construct a JobSubmission instance with an input file shadowing reserved file name $it")
        }
      }

      return JobSubmission(jobID!!, config, inputs)
    }
  }
}