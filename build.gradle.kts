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

  // Jackson
  implementation(platform("com.fasterxml.jackson:jackson-bom:2.13.3"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("org.veupathdb.lib:jackson-singleton:3.0.0")

  // DB
  implementation("com.zaxxer:HikariCP:5.0.1")
  implementation("org.postgresql:postgresql:42.3.6")

  // S3
  implementation("org.veupathdb.lib.s3:s34k-minio:0.3.1+s34k-0.7.0")
  implementation("org.veupathdb.lib.s3:workspaces:3.0.0")

  // Rabbit
  implementation("org.veupathdb.lib:rabbit-job-queue:1.1.0")

  // Metrics
  implementation("io.prometheus:simpleclient:0.15.0")
  implementation("io.prometheus:simpleclient_common:0.15.0")

  // Misc & Utils
  api("org.veupathdb.lib:hash-id:1.0.2")
  implementation("io.foxcapades.lib:env-access:1.0.0")

  // Testing
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}