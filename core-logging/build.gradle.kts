plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(dependency.plugins.kotlin.serialization)
}

android {
    namespace = "com.msa.core.logging"
    compileSdk = 35

    defaultConfig {
        minSdk = 27
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    api(project(":core-common"))
    implementation(dependency.kotlinx.serialization.json)
    implementation(dependency.timber.log)
}