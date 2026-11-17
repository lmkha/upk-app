import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Values that differ per environment live in secrets.properties (git-ignored).
// local.properties is left to Android Studio (sdk.dir).
val secretsFile = rootProject.file("secrets.properties")
if (!secretsFile.isFile) {
    throw GradleException(
        "secrets.properties not found. Run scripts/use-env.sh dev (scripts\\use-env.bat dev on Windows) " +
            "to create it from the secrets folder. See README.md."
    )
}
val secretProperties = Properties().apply {
    secretsFile.inputStream().use(::load)
}

// ENVIRONMENT decides the identity of the installed app. It is mandatory, so a build can
// never fall back to the production applicationId by accident.
val environment = secretProperties.getProperty("ENVIRONMENT")?.trim()?.removeSurrounding("\"")
val (environmentIdSuffix, environmentVersionSuffix, environmentAppName) = when (environment) {
    "dev" -> Triple(".dev", "-dev", "UPK Dev")
    "staging" -> Triple(".staging", "-staging", "UPK Staging")
    "prod" -> Triple(null, null, "UPK")
    else -> throw GradleException(
        "ENVIRONMENT in secrets.properties must be dev, staging or prod, but was '$environment'."
    )
}

android {
    namespace = "com.example.upk"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.upk"
        applicationIdSuffix = environmentIdSuffix
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        versionNameSuffix = environmentVersionSuffix
        resValue("string", "app_name", environmentAppName)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        resValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)
    ksp(libs.kotlin.metadata.jvm)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.stripe.android)
    implementation(libs.financial.connections)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    implementation(libs.retrofit)
    implementation(libs.converter.kotlinx.serialization)

    implementation(libs.androidx.core.splashscreen)

    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:analytics"))
    implementation(project(":core:crashlytics"))
    implementation(project(":feature:auth:data"))
    implementation(project(":feature:home:ui"))
    implementation(project(":feature:settings:ui"))
    implementation(project(":feature:settings:domain"))
    implementation(project(":feature:settings:data"))
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
    }
}
