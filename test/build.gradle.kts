plugins {
  kotlin("jvm")
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
  implementation(project(":compute-platform"))

  implementation("org.veupathdb.lib.s3:s34k-minio:0.7.2+s34k-0.11.0")
  implementation("org.apache.logging.log4j:log4j-core:2.19.0")
  runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
}

tasks.shadowJar {
  archiveFileName.set("test.jar")

  manifest {
    attributes["Main-Class"] = "lcp.MainKt"
  }
}