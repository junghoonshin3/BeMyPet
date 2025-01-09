import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation
import java.io.FileInputStream
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

android {
    namespace = "kr.sjh.bemypet"

    defaultConfig {
        applicationId = "kr.sjh.bemypet"
        versionCode = 1
        versionName = "1.0"
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
//    implementation(project(":core:firebase"))
    implementation(project(":feature:adoption"))
    implementation(project(":feature:setting"))
    implementation(project(":feature:adoption-detail"))
    implementation(project(":feature:favourite"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))

}
