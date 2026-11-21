plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.protobuf.gradle.plugin)
    implementation(libs.secrets.gradle.plugin)
}

kotlin {
    jvmToolchain(17)
}
