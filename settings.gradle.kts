pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    }
}

plugins {
    // برای دانلود خودکار Toolchain های جاوا
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        }
    }

    versionCatalogs{
        create("dependency"){
            from(files("gradle/dependency.versions.toml"))
        }
    }
}

rootProject.name = "Pakhsh-Mobile"
include(":app")
include(":core")
include(":persistence")
include(":visitApp")
include(":distributeApp")
include(":supervisorApp")
include(":core-media")
include(":core-ui")
include(":core-common")
include(":core-di")
include(":core-flags")
include(":core-logging")
include(":core-network")
include(":core-storage")
include(":core-validation")
