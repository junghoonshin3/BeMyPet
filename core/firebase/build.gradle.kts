plugins {
    alias(libs.plugins.bemypet.android.library)
}

android {
    namespace = "kr.sjh.firebase"
}

dependencies {
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
}