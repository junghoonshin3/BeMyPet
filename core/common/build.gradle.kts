import kr.sjh.convention.ext.androidTestImplementation

plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

android {
    namespace = "kr.sjh.core.common"
}

dependencies {
    implementation(libs.androidx.compose.material3)
    androidTestImplementation(libs.androidx.espresso.core)
}