plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

android {
    namespace = "kr.sjh.feature.adoption_detail"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:ktor"))
    implementation(project(":core:common"))
    implementation(libs.coil.compose)
    implementation(libs.airbnb.lottie)
    implementation(libs.maps.compose)
}