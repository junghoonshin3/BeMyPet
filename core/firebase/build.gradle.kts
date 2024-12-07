import kr.sjh.convention.ext.androidTestImplementation

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.bemypet.android.application.firebase)
}

android {
    namespace = "kr.sjh.core.firebase"

}


dependencies {
    implementation(project(":core:model"))
    androidTestImplementation(libs.androidx.espresso.core)
}