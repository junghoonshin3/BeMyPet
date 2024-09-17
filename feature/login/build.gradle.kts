plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

android {
    namespace = "kr.sjh.feature.login"

}

dependencies {
    implementation(project(":core:google"))
    implementation(project(":core:data"))
    implementation(project(":core:firebase"))
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth)
}