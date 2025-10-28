plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}
android {
    namespace = "com.msa.core.storage"
    compileSdk = 35

    defaultConfig {
        minSdk = 27
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
dependencies {
    implementation(project(":core-common"))
    implementation(project(":core-network"))

    implementation(libs.androidx.core.ktx)
    implementation(dependency.google.gson)
    implementation(dependency.security.crypto)
    implementation(dependency.tink.android)
    implementation(dependency.coroutines.android)
    implementation(dependency.timber.log)
}
