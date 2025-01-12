import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
}

android {
    namespace = "kr.sjh.datastore"
}

dependencies {
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.androidx.dataStore.core)
}