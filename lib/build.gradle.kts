import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  id("org.jetbrains.dokka") version "1.7.20"
  java
  `maven-publish`
}

group = "org.veupathdb.lib"
version = "1.5.2"


dependencies {

  // Logging
  implementation("org.slf4j:slf4j-api:1.7.36")

  // Jackson
  implementation(platform("com.fasterxml.jackson:jackson-bom:2.15.0"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("org.veupathdb.lib:jackson-singleton:3.0.0")

  // DB
  implementation("com.zaxxer:HikariCP:5.0.1")
  implementation("org.postgresql:postgresql:42.5.4")

  // S3
  implementation("org.veupathdb.lib.s3:s34k-minio:0.5.0+s34k-0.9.0")
  implementation("org.veupathdb.lib.s3:workspaces:4.1.1")

  // Rabbit
  implementation("org.veupathdb.lib:rabbit-job-queue:1.2.0")

  // Metrics
  implementation("io.prometheus:simpleclient:0.16.0")
  implementation("io.prometheus:simpleclient_common:0.16.0")

  // Misc & Utils
  api("org.veupathdb.lib:hash-id:1.0.2")

  // Testing
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(17)
}

java {
  withJavadocJar()
  withSourcesJar()
}

tasks.javadoc {
  // This is required to generate the javadoc jar when publishing a release.
  exclude("module-info.java")
}

tasks.dokkaHtml {
  outputDirectory.set(file("build/docs/dokka"))
}

tasks.dokkaJavadoc {
  outputDirectory.set(file("build/docs/javadoc"))
}

task("docs") {
  dependsOn("dokkaHtml", "dokkaJavadoc")
}

publishing {
  repositories {
    maven {
      name = "GitHub"
      url = uri("https://maven.pkg.github.com/VEuPathDB/lib-compute-platform")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
      }
    }
  }

  publications {
    create<MavenPublication>("gpr") {
      from(components["java"])
      pom {
        name.set("S3 Workspaces")
        description.set("Workspaces backed by an S3 object store.")
        url.set("https://github.com/VEuPathDB/lib-compute-platform")
        developers {
          developer {
            id.set("epharper")
            name.set("Elizabeth Paige Harper")
            email.set("epharper@upenn.edu")
            url.set("https://github.com/foxcapades")
            organization.set("VEuPathDB")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/VEuPathDB/lib-compute-platform.git")
          developerConnection.set("scm:git:ssh://github.com/VEuPathDB/lib-compute-platform.git")
          url.set("https://github.com/VEuPathDB/lib-compute-platform")
        }
      }
    }
  }
}
