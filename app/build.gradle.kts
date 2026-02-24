import com.android.build.gradle.internal.tasks.factory.registerTask
import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    alias(libs.plugins.bemypet.android.application)
    alias(libs.plugins.bemypet.android.application.compose)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.bemypet.android.application.firebase)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.crashlytics)
    id("kotlin-parcelize")
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

val versionProps = Properties().apply {
    load(FileInputStream(rootProject.file("version.properties")))
}

android {
    namespace = "kr.sjh.bemypet"
    val versionPropsFile = rootProject.file("version.properties")
    var versionBuildCode: Int
    var versionBuildMajor: String
    var versionBuildMinor: String
    var versionBuildPatch: String
    var newVersionName: String
    if (versionPropsFile.canRead()) {
        val versionProps = Properties()
        versionProps.load(FileInputStream(versionPropsFile))
        versionBuildCode = versionProps["VERSION_CODE"].toString().toInt()
        versionBuildMajor = versionProps["VERSION_MAJOR"].toString()
        versionBuildMinor = versionProps["VERSION_MINOR"].toString()
        versionBuildPatch = versionProps["VERSION_PATCH"].toString()
        newVersionName = "$versionBuildMajor.$versionBuildMinor.$versionBuildPatch"
        System.out.println("versionName : $newVersionName")
        System.out.println("versionCode : $versionBuildCode")
    } else {
        throw FileNotFoundException("Could not read version.properties")
    }

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            versionNameSuffix = "-dev"
            applicationIdSuffix = ".debug"
            manifestPlaceholders["APP_NAME"] = "@string/app_name_dev"
        }

        create("prod") {
            dimension = "env"
            // suffix 없음 → 스토어용
            manifestPlaceholders["APP_NAME"] = "@string/app_name"
        }
    }

    defaultConfig {
        applicationId = "kr.sjh.bemypet"
        versionCode = versionBuildCode
        versionName = newVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(prodSecrets.requireKey("STORE_FILE"))
            keyAlias = prodSecrets.requireKey("KEY_ALIAS")
            keyPassword = prodSecrets.requireKey("KEY_PASSWORD")
            storePassword = prodSecrets.requireKey("STORE_PASSWORD")
        }
    }

    buildTypes {
        debug {

            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
            manifestPlaceholders["MAPS_API_KEY"] = devSecrets.requireKey("MAPS_API_KEY")
            manifestPlaceholders["AD_ID"] = devSecrets.requireKey("AD_ID")
            manifestPlaceholders["AUTH_SCHEME"] = "bemypet-dev"
            manifestPlaceholders["AUTH_HOST"] = "oauth"
        }

        release {
            ndk {
                debugSymbolLevel = "FULL"
            }
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")

            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
            manifestPlaceholders["MAPS_API_KEY"] = prodSecrets.requireKey("MAPS_API_KEY")
            manifestPlaceholders["AD_ID"] = prodSecrets.requireKey("AD_ID")
            manifestPlaceholders["AUTH_SCHEME"] = "bemypet"
            manifestPlaceholders["AUTH_HOST"] = "oauth"
        }
    }

    composeCompiler {
        enableStrongSkippingMode = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

androidComponents {
    beforeVariants(selector().all()) { variant ->
        val env = variant.productFlavors.firstOrNull { it.first == "env" }?.second
        variant.enable =
            (env == "dev" && variant.buildType == "debug") ||
                (env == "prod" && variant.buildType == "release")
    }
}

dependencies {
    implementation(libs.core.splashscreen)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    implementation(libs.coil.compose)
    implementation(libs.play.services.ads)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth.kt)
    implementation(project(":feature:adoption"))
    implementation(project(":feature:setting"))
    implementation(project(":feature:adoption-detail"))
    implementation(project(":feature:favourite"))
    implementation(project(":feature:signIn"))
    implementation(project(":feature:comments"))
    implementation(project(":feature:report"))
    implementation(project(":feature:block"))
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:datastore"))
    implementation(project(":core:supabase"))
}
