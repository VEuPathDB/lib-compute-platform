import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.21"
  id("org.jetbrains.dokka") version "1.6.21"
  java
  `maven-publish`
}

group = "org.veupathdb.lib"
version = "1.3.4"

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

  // Jackson
  implementation(platform("com.fasterxml.jackson:jackson-bom:2.13.4"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("org.veupathdb.lib:jackson-singleton:3.0.0")

  // DB
  implementation("com.zaxxer:HikariCP:5.0.1")
  implementation("org.postgresql:postgresql:42.5.0")

  // S3
  implementation("org.veupathdb.lib.s3:s34k-minio:0.3.6+s34k-0.7.2")
  implementation("org.veupathdb.lib.s3:workspaces:4.0.4")

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

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "17"
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17

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
