import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.bemypet.android.library)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "kr.sjh.core.supabase"
    defaultConfig {
        val properties = Properties()
        properties.load(FileInputStream(rootProject.file("secrets.properties")))
        buildConfigField(
            "String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("SUPABASE_ANON_KEY")}\""
        )
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL")}\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth.kt)
    implementation(libs.supabase.realtime.kt)
    implementation(libs.supabase.postgrest.kt)
    implementation(libs.supabase.functions.kt)
    implementation(project(":core:ktor"))
}