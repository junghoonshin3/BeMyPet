plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

android {
    namespace = "kr.sjh.feature.favourite"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(libs.coil.compose)
    implementation(libs.airbnb.lottie)
}