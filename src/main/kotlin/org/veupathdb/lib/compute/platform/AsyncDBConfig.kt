package org.veupathdb.lib.compute.platform

data class AsyncDBConfig @JvmOverloads constructor(
  internal val host: String,
  internal val port: Int,
  internal val user: String,
  internal val pass: String,
  internal val name: String,
  internal val poolSize: Int = 10
)