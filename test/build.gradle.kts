plugins {
  kotlin("jvm")
  id("com.gradleup.shadow") version "9.3.0"
}

dependencies {
  implementation(project(":compute-platform"))

  implementation(libs.jackson)
  implementation("org.veupathdb.lib.s3:s34k-minio:0.7.2+s34k-0.11.0")
  implementation("org.apache.logging.log4j:log4j-core:2.25.2")
  runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.25.2")
}

tasks.shadowJar {
  archiveFileName.set("test.jar")

  manifest {
    attributes["Main-Class"] = "lcp.MainKt"
  }
}