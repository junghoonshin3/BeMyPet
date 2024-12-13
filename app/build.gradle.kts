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
    id("kotlin-parcelize")
}

// properties 파일 로드
val properties = Properties().apply {
    load(FileInputStream(rootProject.file("apikey.properties")))
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

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["APP_NAME"] = "@string/app_name_dev"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            manifestPlaceholders["APP_NAME"] = "@string/app_name"
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
    implementation(project(":feature:adoption"))
    implementation(project(":feature:adoption-detail"))
    implementation(project(":feature:favourite"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
}
