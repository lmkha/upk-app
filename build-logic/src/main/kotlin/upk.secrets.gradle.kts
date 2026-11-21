import com.android.build.api.dsl.CommonExtension

plugins {
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

pluginManager.withPlugin("com.android.base") {
    extensions.configure<CommonExtension> {
        buildFeatures.buildConfig = true
    }
}

secrets {
    propertiesFileName = "secrets.properties"
}
