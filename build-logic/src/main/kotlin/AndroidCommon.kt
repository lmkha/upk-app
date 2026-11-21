import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion

private const val COMPILE_SDK = 37
private const val COMPILE_SDK_MINOR = 2
private const val MIN_SDK = 29
internal const val TARGET_SDK = 37

internal fun CommonExtension.configureAndroidCommon() {
    compileSdk {
        version = release(COMPILE_SDK) {
            minorApiLevel = COMPILE_SDK_MINOR
        }
    }

    defaultConfig.apply {
        minSdk = MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
