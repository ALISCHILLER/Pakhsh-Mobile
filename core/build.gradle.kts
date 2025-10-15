plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(dependency.plugins.kotlin.serialization)
    alias(dependency.plugins.ksp)
}

android {
    namespace = "com.zar.core"
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
        buildConfig = true
    }
}

dependencies {
    // ---- libs (پایه اندروید/کامپوز/تست)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ---- dependency (دامین/شبکه/DI/DB/…)
    // Koin
    implementation(dependency.koin.androidx.compose)
    implementation(dependency.koin.test)
    testImplementation(dependency.koin.android.test)

    // Media3
    implementation(dependency.media3.exoplayer)
    implementation(dependency.media3.ui)
    implementation(dependency.media3.common)

    // Ktor (BOM + ماژول‌ها)
    implementation(platform(dependency.ktor.bom))
    implementation(dependency.ktor.core)
    implementation(dependency.ktor.android)
    implementation(dependency.ktor.okhttp)
    implementation(dependency.ktor.logging)
    implementation(dependency.ktor.negotiation)
    implementation(dependency.ktor.json)
    implementation(dependency.ktor.auth)
    implementation(dependency.ktor.resources)



    // Coroutines / Timber
    implementation(dependency.coroutines.android)
    implementation(dependency.timber.log)

    // Coil v3
    implementation(dependency.coil.image)

    // Room + KSP
    implementation(dependency.room.runtime)
    implementation(dependency.room.ktx)
    ksp(dependency.room.compiler)

    // CameraX
    implementation(dependency.camera.core)
    implementation(dependency.camera.lifecycle)
    implementation(dependency.camera.view)

    // Accompanist Permissions
    implementation(dependency.accompanist.permissions)

    // Security: AndroidX + Tink
    implementation(dependency.security.crypto)
    implementation(dependency.tink.android)

    // Gson
    implementation(dependency.google.gson)
}
