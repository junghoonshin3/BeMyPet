# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.

# Ktor 관련 클래스 유지
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class io.ktor.client.plugins.** { *; }

# Kotlin Coroutines 관련 유지 (필요할 수도 있음)
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

-keepnames class kr.sjh.core.model.ReportType
-keep class kr.sjh.core.model.ReportType { *; }
