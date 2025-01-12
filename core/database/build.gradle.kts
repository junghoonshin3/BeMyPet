import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "kr.sjh.database"
    room {
        schemaDirectory("debug","${projectDir.path}/schemas/debug")
        schemaDirectory("release","${projectDir.path}/schemas/release")
    }
}

dependencies {
    implementation(project(":core:model"))
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
}