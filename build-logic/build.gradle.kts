plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.protobuf.gradle.plugin)
}

kotlin {
    jvmToolchain(17)
}
