import com.google.protobuf.gradle.ProtobufExtension
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("com.google.protobuf")
}

val libs = extensions
    .getByType(VersionCatalogsExtension::class.java)
    .named("libs")

val protobufVersion = libs
    .findVersion("protobufKotlinLite")
    .get()
    .requiredVersion

extensions.configure<ProtobufExtension> {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }

                create("kotlin")
            }
        }
    }
}