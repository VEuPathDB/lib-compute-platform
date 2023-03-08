package lcp

import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.AsyncPlatform
import org.veupathdb.lib.compute.platform.config.*
import org.veupathdb.lib.compute.platform.job.JobContext
import org.veupathdb.lib.compute.platform.job.JobExecutor
import org.veupathdb.lib.compute.platform.job.JobResult

private val Log = LoggerFactory.getLogger("main.kt")

fun main() {
  initPlatform()

  Log.info("{}", AsyncPlatform.listJobReferences())
}

private fun initPlatform() {
  AsyncPlatform.init(AsyncPlatformConfig.builder()
    .dbConfig(AsyncDBConfig.builder()
      .dbName("postgres")
      .host("localhost")
      .port(5432)
      .username("postgres")
      .password("password")
      .poolSize(1)
      .build())
    .s3Config(AsyncS3Config("localhost", 9000, false, "derp", "minioadmin", "minioadmin", "flumps"))
    .addQueue(AsyncQueueConfig("queue", "guest", "guest", "localhost", 5672, 1))
    .jobConfig(AsyncJobConfig({ Executor() }, 1))
    .localWorkspaceRoot("/tmp/florp")
    .build())
}

class Executor : JobExecutor {
  private val log = LoggerFactory.getLogger(javaClass)

  override fun execute(ctx: JobContext): JobResult {
    log.info("I'm job {}!", ctx.jobID)
    return JobResult.success()
  }
}