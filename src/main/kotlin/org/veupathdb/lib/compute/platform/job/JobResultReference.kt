package org.veupathdb.lib.compute.platform.job

import java.io.InputStream

/**
 * Job Result Reference
 *
 * Provides data about and access to an individual job result file.
 *
 * @author Elizabeth Paige Harper [https://github.com/foxcapades]
 * @since 1.0.0
 */
interface JobResultReference {

  /**
   * Basename of the result file.
   */
  val name: String

  /**
   * Size of the result file in bytes.
   */
  val size: Long

  /**
   * Opens a stream over the contents of the result file.
   *
   * @return A stream over the contents of the result file.
   */
  fun open(): InputStream
}