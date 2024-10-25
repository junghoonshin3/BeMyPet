import kr.sjh.convention.ext.implementation

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.library.compose)
}

android {
    namespace = "kr.sjh.core.designsystem"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.coil.compose)
}