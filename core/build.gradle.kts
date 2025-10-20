plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(project(":core-common"))
    // ---- libs (پایه اندروید/تست)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ---- dependency (دامین/شبکه/DI/DB/…)
    // Koin
    implementation(dependency.koin.android)
    implementation(dependency.koin.test)
    testImplementation(dependency.koin.android.test)



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



    // Room + KSP
    implementation(dependency.room.runtime)
    implementation(dependency.room.ktx)
    ksp(dependency.room.compiler)


    // Security: AndroidX + Tink
    implementation(dependency.security.crypto)
    implementation(dependency.tink.android)

    // Gson
    implementation(dependency.google.gson)
}
