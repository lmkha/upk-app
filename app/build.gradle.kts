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
class EnvironmentConfig(
    val applicationIdSuffix: String?,
    val versionNameSuffix: String?,
    val appName: String,
)

val environment = secretProperties.getProperty("ENVIRONMENT")?.trim()
val environmentConfig = when (environment) {
    "dev" -> EnvironmentConfig(
        applicationIdSuffix = ".dev",
        versionNameSuffix = "-dev",
        appName = "UPK Dev",
    )
    "staging" -> EnvironmentConfig(
        applicationIdSuffix = ".staging",
        versionNameSuffix = "-staging",
        appName = "UPK Staging",
    )
    "prod" -> EnvironmentConfig(
        applicationIdSuffix = null,
        versionNameSuffix = null,
        appName = "UPK",
    )
    else -> throw GradleException(
        "ENVIRONMENT in secrets.properties must be dev, staging or prod, but was '$environment'."
    )
}

// Release builds are signed with a keystore that stays outside the repository. Its path and
// passwords come from environment variables, never from secrets.properties: every key of that
// file is compiled into BuildConfig, and so into the APK. See "Release signing" in README.md.
val releaseSigningValues = listOf(
    "UPK_RELEASE_KEYSTORE_FILE",
    "UPK_RELEASE_KEYSTORE_PASSWORD",
    "UPK_RELEASE_KEY_ALIAS",
    "UPK_RELEASE_KEY_PASSWORD",
).associateWith { name ->
    providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }
}
val missingReleaseSigningVariables = releaseSigningValues.filterValues { it == null }.keys.toList()

android {
    namespace = "com.example.upk"
    compileSdk {
        version = release(37)
    }

    signingConfigs {
        create("release") {
            storeFile = releaseSigningValues["UPK_RELEASE_KEYSTORE_FILE"]?.let { file(it) }
            storePassword = releaseSigningValues["UPK_RELEASE_KEYSTORE_PASSWORD"]
            keyAlias = releaseSigningValues["UPK_RELEASE_KEY_ALIAS"]
            keyPassword = releaseSigningValues["UPK_RELEASE_KEY_PASSWORD"]
        }
    }

    defaultConfig {
        applicationId = "com.example.upk"
        applicationIdSuffix = environmentConfig.applicationIdSuffix
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        versionNameSuffix = environmentConfig.versionNameSuffix
        resValue("string", "app_name", environmentConfig.appName)

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
            signingConfig = signingConfigs.getByName("release")
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
    androidTestImplementation(platform(libs.androidx.compose.bom))
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

// A release build never falls back to the debug key: without the signing variables it stops
// here. Debug builds and tests do not run this task. R8 and the Crashlytics mapping upload
// depend on the check, so a build that cannot be signed skips them and uploads nothing, also
// when the build keeps going after a failure (--continue, the mode Android Studio builds in).
tasks.named { it == "minifyReleaseWithR8" || it == "uploadCrashlyticsMappingFileRelease" }.configureEach {
    dependsOn("validateSigningRelease")
}
tasks.named { it == "validateSigningRelease" }.configureEach {
    val missingVariables = missingReleaseSigningVariables
    doFirst {
        if (missingVariables.isNotEmpty()) {
            throw GradleException(
                "Release builds need a signing keystore. Set the environment variables " +
                    "${missingVariables.joinToString()}. See \"Release signing\" in README.md."
            )
        }
    }
}
