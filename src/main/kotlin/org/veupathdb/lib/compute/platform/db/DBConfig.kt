package org.veupathdb.lib.compute.platform.db

import io.foxcapades.lib.env.NBEnv

/**
 * Database Configuration Options
 */
internal object DBConfig {

  /**
   * Postgres root username.
   */
  @JvmStatic
  val pgRootUser = NBEnv.require("PG_ROOT_USER")

  /**
   * Postgres root password.
   */
  @JvmStatic
  val pgRootPassword = NBEnv.require("PG_ROOT_PASS")

  /**
   * Postgres port number.
   *
   * Defaults to `5432`.
   */
  @JvmStatic
  val pgPort = NBEnv.get("PG_PORT") { it -> it.toInt() } ?: 5432

  /**
   * Postgres host name.
   */
  @JvmStatic
  val pgHost = NBEnv.require("PG_HOST")

  /**
   * Postgres driver log level.
   *
   * A value of `1` means `INFO` level logging and higher.
   * A value of `1` means `DEBUG` level logging and higher.
   *
   * Defaults to `1`.
   */
  @JvmStatic
  val pgLogLevel = NBEnv.get("PG_LOG_LEVEL") { it -> it.toInt() } ?: 1

  /**
   * Postgres database name.
   *
   * Defaults to `service`.
   */
  @JvmStatic
  val pgDBName = NBEnv["PG_DB_NAME"] ?: "service"

  /**
   * Postgres debug username.
   *
   * Defaults to `pg_debug`
   */
  @JvmStatic
  val pgDebugUser = NBEnv["PG_DEBUG_USER"] ?: "pg_debug"

  /**
   * Postgres debug password.
   *
   * Defaults to `4a40499dcf56cac2d8ceaeaf26053f96`.
   */
  @JvmStatic
  val pgDebugPass = NBEnv["PG_DEBUG_PASS"] ?: "4a40499dcf56cac2d8ceaeaf26053f96"
}