import kr.sjh.convention.ext.implementation

plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

android {
    namespace = "kr.sjh.setting"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(libs.coil.compose)
    implementation(libs.airbnb.lottie)

    testImplementation(libs.kotlinx.coroutines.test)
}
