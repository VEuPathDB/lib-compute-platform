package org.veupathdb.lib.compute.platform

import java.io.InputStream

interface JobResultReference {
  val name: String

  val size: Long

  fun open(): InputStream
}