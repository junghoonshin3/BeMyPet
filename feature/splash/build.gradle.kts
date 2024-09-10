plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
    alias(libs.plugins.bemypet.android.application.firebase)
}

android {
    namespace = "kr.sjh.feature.splash"
}

dependencies {
    implementation(libs.core.splashscreen)
    implementation(project(":core:firebase"))
}