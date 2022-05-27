package org.veupathdb.lib.compute.platform.conf

private const val DefaultDebugUser = "queue_db_debug"
private const val DefaultDebugPass = "4a40499dcf56cac2d8ceaeaf26053f96"

data class AsyncDBConfig(
  internal val host: String,
  internal val port: Int,
  internal val user: String,
  internal val pass: String,
  internal val name: String,
  internal val debugUser: String,
  internal val debugPass: String,
) {

  constructor(host: String, port: Int, user: String, pass: String, name: String)
    : this(host, port, user, pass, name, DefaultDebugUser, DefaultDebugPass)
}