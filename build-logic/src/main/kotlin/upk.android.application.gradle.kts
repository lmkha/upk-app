plugins {
    id("com.android.application")
}

android {
    configureAndroidCommon(libs)

    defaultConfig {
        targetSdk = libs.intVersion("targetSdk")
    }
}
