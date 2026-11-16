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
    // Compile-only: the root build already puts the Android Gradle Plugin on the classpath.
    compileOnly(libs.android.gradle.api)
}

kotlin {
    jvmToolchain(17)
}
