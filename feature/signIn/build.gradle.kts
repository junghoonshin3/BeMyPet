import kr.sjh.convention.ext.implementation

plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

android {
    namespace = "kr.sjh.feature.signup"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(libs.coil.compose)

    testImplementation(libs.kotlinx.coroutines.test)
}
