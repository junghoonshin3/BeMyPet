import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)

}

android {
    namespace = "kr.sjh.data"
}

dependencies {
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(project(":core:model"))
    implementation(project(":core:ktor"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:supabase"))
    implementation(libs.ktor.serialization.kotlinx.json)
}