plugins {
    alias(libs.plugins.bemypet.android.application)
    alias(libs.plugins.bemypet.android.application.compose)
    alias(libs.plugins.bemypet.android.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.bemypet.android.application.firebase)

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
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
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
    implementation(project(":feature:splash"))
    implementation(project(":core:designsystem"))
}