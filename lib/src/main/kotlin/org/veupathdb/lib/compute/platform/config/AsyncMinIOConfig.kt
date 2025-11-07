package org.veupathdb.lib.compute.platform.config

import org.veupathdb.lib.compute.platform.intern.minio.MinIOHax
import kotlin.time.Duration

class AsyncMinIOConfig(
  host: String,
  port: Int,
  https: Boolean,
  bucket: String,
  accessToken: String,
  secretKey: String,
  rootPath: String,

  maxDeleteAwaitTime: Duration,
  retrySleepTime: Duration,
  retryCount: Int,
): AsyncS3Config(
  host = host,
  port = port,
  https = https,
  bucket = bucket,
  accessToken = accessToken,
  secretKey = secretKey,
  rootPath = rootPath,
) {
  init {
    MinIOHax.MaxDeleteAwaitTime = maxDeleteAwaitTime
    MinIOHax.RetrySleepTime = retrySleepTime
    MinIOHax.RetryCount = retryCount
  }

  @Suppress("unused")
  constructor(host: String, bucket: String, access: String, secret: String):
    this(host, DefaultPort, DefaultHTTPS, bucket, access, secret, DefaultRootPath)

  @Suppress("unused")
  constructor(host: String, bucket: String, access: String, secret: String, root: String):
    this(host, DefaultPort, DefaultHTTPS, bucket, access, secret, root)

  @Suppress("unused")
  private constructor(
    base: AsyncS3Config,
    maxDeleteAwaitTime: Duration,
    retrySleepTime: Duration,
    retryCount: Int,
  ): this(
    base.host,
    base.port,
    base.https,
    base.bucket,
    base.accessToken,
    base.secretKey,
    base.rootPath,
    maxDeleteAwaitTime,
    retrySleepTime,
    retryCount,
  )

  constructor(
    host: String,
    port: Int,
    https: Boolean,
    bucket: String,
    accessToken: String,
    secretKey: String,
    rootPath: String,
  ): this (
    host               = host,
    port               = port,
    https              = https,
    bucket             = bucket,
    accessToken        = accessToken,
    secretKey          = secretKey,
    rootPath           = rootPath,
    maxDeleteAwaitTime = MinIOHax.MaxDeleteAwaitTime,
    retrySleepTime     = MinIOHax.RetrySleepTime,
    retryCount         = MinIOHax.RetryCount,
  )

  class Builder: AsyncS3Config.Builder() {
    var maxDeleteAwaitTime = MinIOHax.MaxDeleteAwaitTime
    var retrySleepTime = MinIOHax.RetrySleepTime
    var retryCount = MinIOHax.RetryCount

    fun maxDeleteAwaitTime(value: Duration) =
      apply { maxDeleteAwaitTime = value }

    fun retrySleepTime(value: Duration) =
      apply { retrySleepTime = value }

    fun retryCount(value: Int) =
      apply { retryCount = value }

    override fun build() =
      AsyncMinIOConfig(super.build(), maxDeleteAwaitTime, retrySleepTime, retryCount)
  }
}