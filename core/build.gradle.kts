plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.msa.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 27

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true // فعال کردن BuildConfig
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.compose.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.material3)

    //di koin
    implementation(dependency.koin.androidx.compose)
    implementation(dependency.koin.test)
    testImplementation(dependency.koin.android.test)

    //  exoplayer
    implementation(dependency.media3.exoplayer)
    implementation(dependency.media3.ui)
    implementation(dependency.media3.common)

    //network ktor
    implementation(platform(dependency.ktor.bom))
    implementation(dependency.ktor.android)
    implementation(dependency.ktor.serialization)
    implementation(dependency.ktor.logging)
    implementation(dependency.ktor.negotiation)
    implementation(dependency.ktor.json)
    implementation(dependency.ktor.okhttp)

    //coroutines
    implementation(dependency.coroutines.android)
    // log  timber
    implementation(dependency.timber.log)

    // image loader coil
    implementation(dependency.coil.image)

    //DB Room
    implementation(dependency.room.runtime)
    ksp(dependency.room.compiler)
    implementation(dependency.room.ktx)

    // CameraX
    implementation(dependency.camera.core)
    implementation(dependency.camera.lifecycle)
    implementation(dependency.camera.view)

    // Permissions
    implementation(dependency.accompanist.permissions)

    //security
    implementation(dependency.security.crypto)
    // implementation("androidx.security:security-state:1.0.0")

    // google gson
    implementation(dependency.google.gson)
}