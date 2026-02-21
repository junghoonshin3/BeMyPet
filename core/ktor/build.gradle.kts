import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation
import java.util.Properties

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.bemypet.ktor)
    alias(libs.plugins.kotlin.serialization)
}

fun loadSecrets(fileName: String): Properties = Properties().apply {
    val file = rootProject.file(fileName)
    check(file.exists()) { "Missing secrets file: $fileName" }
    file.inputStream().use { load(it) }
}

fun Properties.requireKey(name: String): String =
    getProperty(name)
        ?.trim()
        ?.trim('"')
        ?.takeIf { it.isNotEmpty() }
        ?: error("Missing key '$name'")

val devSecrets = loadSecrets("secrets.dev.properties")
val prodSecrets = loadSecrets("secrets.prod.properties")

android {
    namespace = "kr.sjh.core.ktor"

    buildFeatures.buildConfig = true

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "SERVICE_KEY",
                "\"${devSecrets.requireKey("SERVICE_KEY")}\""
            )
            buildConfigField(
                "String",
                "BASE_URL",
                "\"${devSecrets.requireKey("BASE_URL")}\""
            )
        }
        release {
            buildConfigField(
                "String",
                "SERVICE_KEY",
                "\"${prodSecrets.requireKey("SERVICE_KEY")}\""
            )
            buildConfigField(
                "String",
                "BASE_URL",
                "\"${prodSecrets.requireKey("BASE_URL")}\""
            )
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    androidTestImplementation(libs.androidx.espresso.core)

}
