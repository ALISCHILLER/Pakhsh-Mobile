// Top-level build file where you can add configuration options common to all sub-projects/modules.
// اختیاری: اگر خواستی، می‌تونی به صورت global هم Toolchain رو ست کنی:
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.JavaVersion
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false

    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}


allprojects {
    // برای ماژول‌های اندرویدی در خودشان هم تنظیم می‌گذاریم (پایین‌تر توضیح دادم)
}