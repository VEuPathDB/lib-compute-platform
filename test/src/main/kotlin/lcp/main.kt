package lcp

import org.slf4j.LoggerFactory
import org.veupathdb.lib.compute.platform.AsyncPlatform
import org.veupathdb.lib.compute.platform.config.*
import org.veupathdb.lib.compute.platform.job.JobContext
import org.veupathdb.lib.compute.platform.job.JobExecutor
import org.veupathdb.lib.compute.platform.job.JobResult
import org.veupathdb.lib.s3.s34k.S3Api
import org.veupathdb.lib.s3.s34k.S3Config
import org.veupathdb.lib.s3.s34k.fields.BucketName
import kotlin.time.Duration.Companion.minutes

private val Log = LoggerFactory.getLogger("main.kt")

fun main() {
  // Init minio
  setupS3ForTests()

  // Init test target
  initPlatform()

  // Run test
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
      .poolSize(5)
      .build())
    .s3Config(AsyncS3Config("localhost", 9000, false, "derp", "minioadmin", "minioadmin", "flumps"))
    .addQueue(AsyncQueueConfig("queue", "guest", "guest", "localhost", 5672, 1, 5.minutes))
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

private fun setupS3ForTests() {
  val client = S3Api.newClient(S3Config("localhost", 9000u, false, "minioadmin", "minioadmin"))
  client.buckets.createIfNotExists(BucketName("derp"))
}