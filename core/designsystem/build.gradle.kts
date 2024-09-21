plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.library.compose)
}

android {
    namespace = "kr.sjh.core.designsystem"
}

dependencies {
    implementation(project(":core:model"))
}