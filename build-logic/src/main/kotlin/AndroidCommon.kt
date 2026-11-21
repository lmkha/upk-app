import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

internal val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

internal fun VersionCatalog.intVersion(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

internal fun CommonExtension.configureAndroidCommon(libs: VersionCatalog) {
    val compileSdkMajor = libs.intVersion("compileSdk")
    val compileSdkMinor = libs.intVersion("compileSdkMinor")

    compileSdk {
        version = release(compileSdkMajor) {
            minorApiLevel = compileSdkMinor
        }
    }

    defaultConfig.apply {
        minSdk = libs.intVersion("minSdk")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
