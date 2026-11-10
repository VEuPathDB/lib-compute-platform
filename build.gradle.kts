plugins {
  alias(libs.plugins.kotlin)
}

fun findParam(vararg keys: String): String {
  for (key in keys) {
    if (extra.has(key)) {
      (extra[key] as String?)
        ?.takeUnless { it.isBlank() }
        ?.also { return it }
    }

    System.getenv(key)
      ?.takeUnless { it.isBlank() }
      ?.also { return it }
  }

  throw IllegalStateException("could not find param for ${keys.asList()}")
}

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
    maven {
      name = "GitHubPackages"
      url  = uri("https://maven.pkg.github.com/veupathdb/maven-packages")
      credentials {
        username = findParam("gpr.user", "GITHUB_USERNAME", "GH_USERNAME")
        password = findParam("gpr.key", "GITHUB_TOKEN", "GH_USERNAME")
      }
    }
  }
}
