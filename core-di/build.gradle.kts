plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}
android {
    namespace = "com.msa.core.di"
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
    implementation(project(":core-storage"))
    implementation(project(":core-logging"))
    implementation(project(":core-validation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(dependency.koin.android)

    implementation(dependency.koin.core)
    implementation(dependency.coroutines.android)
    implementation(dependency.google.gson)
    implementation(dependency.timber.log)
}
