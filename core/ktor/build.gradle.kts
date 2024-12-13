import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.util.Properties

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.bemypet.ktor)
    alias(libs.plugins.kotlin.serialization)
}

val properties = Properties()
properties.load(project.rootProject.file("apikey.properties").inputStream())

android {
    namespace = "kr.sjh.core.ktor"

    //TODO buildConfig Deprecated 될 예정
    android.buildFeatures.buildConfig = true

    defaultConfig {
        buildConfigField("String", "SERVICE_KEY", properties["SERVICE_KEY"].toString())
        buildConfigField(
            "String", "BASE_URL", properties["BASE_URL"].toString()
        )
    }
}

dependencies {
    implementation(project(":core:model"))
    androidTestImplementation(libs.androidx.espresso.core)
}