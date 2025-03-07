import kr.sjh.convention.ext.androidTestImplementation
import kr.sjh.convention.ext.implementation
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.bemypet.android.library.compose)
    alias(libs.plugins.bemypet.android.feature)
}

// properties 파일 로드
val secretsProps = Properties().apply {
    load(FileInputStream(rootProject.file("secrets.properties")))
}

android {
    namespace = "kr.sjh.core.common"
    buildTypes {
        debug {
            buildConfigField(
                "String", "AD_MOB_BANNER_ID", "\"ca-app-pub-3940256099942544/6300978111\""
            )
        }

        release {
            buildConfigField(
                "String", "AD_MOB_BANNER_ID", "\"${secretsProps.getProperty("AD_MOB_BANNER_ID")}\""
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.compose.material3)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.play.services.ads)
}