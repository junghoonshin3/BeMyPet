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

// properties 파일 로드
val properties = Properties().apply {
    load(FileInputStream(rootProject.file("secrets.properties")))
}

val versionPropsFile = Properties().apply {
    load(FileInputStream(rootProject.file("version.properties")))
}

val appName = "BeMyPet"

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

    defaultConfig {
        applicationId = "kr.sjh.bemypet"
        versionCode = versionBuildCode
        versionName = newVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["MAPS_API_KEY"] = properties["MAPS_API_KEY"].toString()

    }

    signingConfigs {
        create("release") {
            storeFile = file(properties["STORE_FILE"].toString())
            keyAlias = properties["KEY_ALIAS"].toString()
            keyPassword = properties["KEY_PASSWORD"].toString()
            storePassword = properties["STORE_PASSWORD"].toString()
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["APP_NAME"] = "@string/app_name_dev"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["APP_NAME"] = "@string/app_name"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
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

dependencies {
    implementation(libs.core.splashscreen)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    implementation(libs.coil.compose)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.ads)

    implementation(project(":feature:adoption"))
    implementation(project(":feature:setting"))
    implementation(project(":feature:adoption-detail"))
    implementation(project(":feature:favourite"))
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:datastore"))

}