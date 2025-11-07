package lcp

import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.veupathdb.lib.compute.platform.AsyncPlatform
import org.veupathdb.lib.compute.platform.config.*
import org.veupathdb.lib.compute.platform.job.JobContext
import org.veupathdb.lib.compute.platform.job.JobExecutor
import org.veupathdb.lib.compute.platform.job.JobResult
import org.veupathdb.lib.compute.platform.job.JobSubmission
import org.veupathdb.lib.hash_id.HashID
import org.veupathdb.lib.jackson.json.Json
import org.veupathdb.lib.s3.s34k.S3Api
import org.veupathdb.lib.s3.s34k.S3Config
import org.veupathdb.lib.s3.s34k.fields.BucketName
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes

private const val QueueName = "queue"

private val Log = LoggerFactory.getLogger("--TEST--")

fun main() {
  // Init minio
  setupS3ForTests()

  // Init test target
  initPlatform()

  // Run tests
  test1()

  exitProcess(0)
}

private fun setupS3ForTests() {
  val client = S3Api.newClient(S3Config("localhost", 9000u, false, "minioadmin", "minioadmin"))
  client.buckets.createIfNotExists(BucketName("derp"))
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
    .addQueue(AsyncQueueConfig(QueueName, "guest", "guest", "localhost", 5672, 1, 5.minutes))
    .jobConfig(AsyncJobConfig({ Executor }, 1))
    .localWorkspaceRoot("/tmp/florp")
    .build())
}

object Executor: JobExecutor {
  private val log = LoggerFactory.getLogger(javaClass)

  override fun execute(ctx: JobContext): JobResult {
    log.info("I'm job {}!", ctx.jobID)

    if (ctx.config?.get("success")?.booleanValue() == true) {
      log.info("I succeeded!")
      return JobResult.success()
    }

    log.info("I failed!")
    return JobResult.failure()
  }
}

private val TestLogDivider = "=".repeat(80)

private fun test1() {
  val name = "test 1"
  val marker = MarkerFactory.getMarker(name)

  Log.info(marker, "")
  Log.info(marker, TestLogDivider)
  Log.info(marker, "running test 1: successful job")
  Log.info(marker, "")

  Log.info(marker, "list jobs before submission")
  Log.info(marker, "{}", AsyncPlatform.listJobReferences())

  val jobID = HashID.ofMD5(name)

  Log.info(marker, "submit job {}", jobID)
  AsyncPlatform.submitJob(
    QueueName,
    JobSubmission.builder()
      .config(Json.newObject { put("success", true) })
      .jobID(jobID)
      .build())

  Log.info(marker, "ensure job {} exists", jobID)
  if (!AsyncPlatform.listJobReferences().any { it.jobID == jobID }) {
    Log.error(marker, "job {} was not found in job reference list!", jobID)
    return
  }


  Log.info(marker, "expire job {}", jobID)
  AsyncPlatform.expireJob(jobID)

  Log.info(marker, "delete job {}", jobID)
  AsyncPlatform.deleteJob(jobID)
}