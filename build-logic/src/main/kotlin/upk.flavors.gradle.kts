import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

// Declares the `environment` flavor dimension (dev, staging, prod) for an Android module.
//
// Every Android module applies this plugin, so all of them are compiled once per
// environment and share the same task names (testDevDebugUnitTest, lintDevDebug, ...).
// That is what lets a module read its own BuildConfig safely: BuildConfig values are
// inlined by the compiler, so a module without flavors would ship one environment's
// values in every build.
//
// Only the dimension and the flavor names live here. Anything specific to one module,
// such as the app's applicationIdSuffix, stays in that module's build file.

val environmentDimension = "environment"
val environments = listOf("dev", "staging", "prod")

pluginManager.withPlugin("com.android.application") {
    extensions.configure<ApplicationExtension> {
        flavorDimensions += environmentDimension
        productFlavors {
            environments.forEach { name ->
                create(name) { dimension = environmentDimension }
            }
        }
    }
}

pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension> {
        flavorDimensions += environmentDimension
        productFlavors {
            environments.forEach { name ->
                create(name) { dimension = environmentDimension }
            }
        }
    }
}
