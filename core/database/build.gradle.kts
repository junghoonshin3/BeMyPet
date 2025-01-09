import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
}

android {
    namespace = "kr.sjh.database"
}

dependencies {
    implementation(project(":core:model"))
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
}