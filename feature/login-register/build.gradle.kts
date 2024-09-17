plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

android {
    namespace = "kr.sjh.feature.login_profile"
}

dependencies {
    implementation(project(":core:designsystem"))
}