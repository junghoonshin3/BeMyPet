import kr.sjh.convention.ext.implementation
import kr.sjh.convention.ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("bemypet.android.library")
                apply("bemypet.android.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            dependencies {
                implementation(project(":core:designsystem"))
                implementation(project(":core:model"))

                implementation(libs.findLibrary("androidx-hilt-navigation-compose").get())
                implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
                implementation(libs.findLibrary("androidx-lifecycle-viewModelCompose").get())
                implementation(libs.findLibrary("kotlinx-coroutines-android").get())
                implementation(libs.findLibrary("kotlinx-collections-immutable").get())
                implementation(libs.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}