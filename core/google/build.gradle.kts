plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.hilt)

}

android {
    namespace = "kr.sjh.core.google"
    android.buildFeatures.buildConfig = true
    defaultConfig {
        proguardFiles("proguard-rules.pro")
        buildConfigField(
            "String",
            "WEB_CLIENT_ID",
            "325989092769-2cllsheph2rtkvt72hukacomlbm5blst.apps.googleusercontent.com"
        )
    }

}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
}