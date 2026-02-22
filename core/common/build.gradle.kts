import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
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
    namespace = "kr.sjh.core.common"
    buildTypes {
        debug {
            buildConfigField(
                "String", "AD_MOB_BANNER_ID", "\"ca-app-pub-3940256099942544/6300978111\""
            )
            buildConfigField(
                "String",
                "WEB_CLIENT_ID",
                "\"${devSecrets.requireKey("WEB_CLIENT_ID")}\""
            )
        }

        release {
            buildConfigField(
                "String",
                "AD_MOB_BANNER_ID",
                "\"${prodSecrets.requireKey("AD_MOB_BANNER_ID")}\""
            )
            buildConfigField(
                "String",
                "WEB_CLIENT_ID",
                "\"${prodSecrets.requireKey("WEB_CLIENT_ID")}\""
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.compose.material3)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.credentials)
    implementation(libs.googleid)
    implementation(libs.androidx.credentials.play.services.auth)
}
