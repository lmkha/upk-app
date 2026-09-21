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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "UPK"
include(":app")
include(":core:ui")
include(":core:data")
include(":core:domain")
include(":feature:payment:ui")
include(":feature:payment:data")
include(":core:common")
include(":feature:settings:ui")
include(":feature:settings:data")
include(":feature:settings:domain")
include(":feature:payment:domain")
include(":feature:auth:data")
include(":feature:auth:domain")
include(":feature:auth:data")
