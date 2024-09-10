plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

android {
    namespace = "kr.sjh.core.login"
}

dependencies {
    implementation(project(":core:designsystem"))
}