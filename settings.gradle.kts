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
    }
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

rootProject.name = "ZarPakhsh-Mobile"
include(":app")
include(":core")
include(":persistenc")
include(":visitApp")
include(":distributeApp")
include(":supervisorApp")
include(":core-media")
include(":core-ui")
