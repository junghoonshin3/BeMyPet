import kr.sjh.convention.ext.androidTestImplementation

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
}

android {
    namespace = "kr.sjh.core.common"
}

dependencies {
    implementation(libs.androidx.compose.material3)
    androidTestImplementation(libs.androidx.espresso.core)
}