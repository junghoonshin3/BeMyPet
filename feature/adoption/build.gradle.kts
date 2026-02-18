plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

android {
    namespace = "kr.sjh.feature.adoption"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:ktor"))
    implementation(project(":core:common"))
    implementation(libs.coil.compose)
    implementation(libs.composables.core)
    implementation(libs.androidx.foundation.layout.android)
    testImplementation(libs.kotlinx.coroutines.test)
}
