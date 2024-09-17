import kr.sjh.convention.ext.implementation
import kr.sjh.convention.ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.gms.google-services")
            }

            dependencies {
                val bom = libs.findLibrary("firebase-bom").get()
                implementation(platform(bom))
                implementation(libs.findLibrary("firebase-auth").get())
                implementation(libs.findLibrary("firebase-firestore").get())
                implementation(libs.findLibrary("firebase-storage").get())
                implementation(libs.findLibrary("play-services-auth").get())
            }
        }
    }
}