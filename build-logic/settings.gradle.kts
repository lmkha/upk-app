pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

// Keep wrapper properties in sync with the root project so Android Studio tasks
// (such as Clean Project) use the same Gradle version as the root build.
val rootWrapperProps = file("../gradle/wrapper/gradle-wrapper.properties")
val localWrapperProps = file("gradle/wrapper/gradle-wrapper.properties")
if (rootWrapperProps.exists() &&
    (!localWrapperProps.exists() || rootWrapperProps.readText() != localWrapperProps.readText())
) {
    rootWrapperProps.copyTo(localWrapperProps, overwrite = true)
}

rootProject.name = "build-logic"
