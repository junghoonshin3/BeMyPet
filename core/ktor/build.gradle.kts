import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.bemypet.ktor)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "kr.sjh.core.ktor"

    //TODO buildConfig Deprecated 될 예정
    android.buildFeatures.buildConfig = true

    defaultConfig {
        buildConfigField(
            "String",
            "SERVER_KEY",
            "\"8BPxw0uKmTc99bwBtMPtEGdsS/IMpbyt3/xzxPnvkukjDGHZ/0b52vOorYq1PNNldA7Ebzz9iUTKwIZZ0H02iQ==\""
        )
        buildConfigField(
            "String", "BASE_URL", "\"http://apis.data.go.kr/1543061/abandonmentPublicSrvc/\""
        )
    }
}

dependencies {
    implementation(project(":core:model"))
    androidTestImplementation(libs.androidx.espresso.core)
}