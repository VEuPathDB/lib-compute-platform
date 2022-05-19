import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.21"
}

group = "org.veupathdb.lib"
version = "1.0-SNAPSHOT"

repositories {
  mavenLocal()
  mavenCentral()
  maven {
    name = "GitHubPackages"
    url  = uri("https://maven.pkg.github.com/veupathdb/maven-packages")
    credentials {
      username = if (extra.has("gpr.user")) extra["gpr.user"] as String? else System.getenv("GITHUB_USERNAME")
      password = if (extra.has("gpr.key")) extra["gpr.key"] as String? else System.getenv("GITHUB_TOKEN")
    }
  }
}

dependencies {
  // Logging
  implementation("org.slf4j:slf4j-api:1.7.36")
  implementation("org.apache.logging.log4j:log4j-core:2.17.2")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")

  // S3
  api("org.veupathdb.lib.s3:s34k-minio:0.3.1+s34k-0.7.0")

  // Rabbit
  api("org.veupathdb.lib:rabbit-job-queue:1.0.1")
  implementation("com.rabbitmq:amqp-client:5.14.0")

  // Job IDs
  api("org.veupathdb.lib:hash-id:1.0.2")

  // Testing
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}