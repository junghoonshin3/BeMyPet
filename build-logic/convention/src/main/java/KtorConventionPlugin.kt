import kr.sjh.convention.ext.implementation
import kr.sjh.convention.ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KtorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                implementation(libs.findLibrary("ktor.client.core").get())
//                implementation(libs.findLibrary("ktor.client.android").get())
                implementation(libs.findLibrary("ktor.client.cio").get())
                implementation(libs.findLibrary("ktor.client.content.negotiation").get())
                implementation(libs.findLibrary("ktor-client-logging").get())
                implementation(libs.findLibrary("ktor.serialization.kotlinx.xml").get())
                implementation(libs.findLibrary("ktor.serialization.kotlinx.json").get())
                implementation(libs.findLibrary("org.slf4j.android").get())
            }
        }
    }
}