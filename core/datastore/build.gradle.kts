plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "kr.sjh.core.datastore"
}

dependencies {
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.androidx.dataStore.core)

}

// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}