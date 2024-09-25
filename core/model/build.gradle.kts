plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.bemypet.android.library.compose)
}

android {
    namespace = "kr.sjh.core.model"
}

dependencies {

    implementation(libs.androidx.compose.runtime)
    implementation(libs.firebase.firestore)
//    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
}