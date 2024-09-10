package kr.sjh.convention

import com.android.build.api.dsl.CommonExtension
import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.debugImplementation
import kr.sjh.convention.ext.implementation
import kr.sjh.convention.ext.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
            buildConfig = true
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()

            implementation(platform(bom))
            androidTestImplementation(platform(bom))
            implementation(libs.findLibrary("androidx-activity-compose").get())
            implementation(libs.findLibrary("androidx-compose-material3").get())
            implementation(libs.findLibrary("androidx-compose-runtime").get())
            debugImplementation(libs.findLibrary("androidx-compose-ui-tooling").get())
            implementation(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            implementation(libs.findLibrary("androidx-compose-ui-util").get())
            androidTestImplementation(libs.findLibrary("androidx-compose-ui-test").get())
            androidTestImplementation(libs.findLibrary("androidx-compose-ui-test-junit4").get())
            debugImplementation(libs.findLibrary("androidx-compose-ui-testManifest").get())
            implementation(libs.findLibrary("androidx-navigation-compose").get())
        }
    }
}