import java.util.Properties

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

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "kr.sjh.core.supabase"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "SUPABASE_ANON_KEY",
                "\"${devSecrets.requireKey("SUPABASE_ANON_KEY")}\""
            )
            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"${devSecrets.requireKey("SUPABASE_URL")}\""
            )
            buildConfigField("String", "SUPABASE_AUTH_SCHEME", "\"bemypet-dev\"")
            buildConfigField("String", "SUPABASE_AUTH_HOST", "\"oauth\"")
        }
        release {
            buildConfigField(
                "String",
                "SUPABASE_ANON_KEY",
                "\"${prodSecrets.requireKey("SUPABASE_ANON_KEY")}\""
            )
            buildConfigField(
                "String",
                "SUPABASE_URL",
                "\"${prodSecrets.requireKey("SUPABASE_URL")}\""
            )
            buildConfigField("String", "SUPABASE_AUTH_SCHEME", "\"bemypet\"")
            buildConfigField("String", "SUPABASE_AUTH_HOST", "\"oauth\"")
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth.kt)
    implementation(libs.supabase.realtime.kt)
    implementation(libs.supabase.postgrest.kt)
    implementation(libs.supabase.functions.kt)
    implementation(libs.supabase.storage.kt)
    implementation(libs.ktor.client.cio)
    implementation(project(":core:ktor"))
}
