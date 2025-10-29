plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(dependency.plugins.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.msa.visitapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.msa.visitapp"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        missingDimensionStrategy("appType", "visit")
        missingDimensionStrategy("envType", "prod")
    }

    flavorDimensions += listOf("appType", "envType")
    productFlavors {
        create("visit") {
            dimension = "appType"
            applicationId = "com.msa.visitapp"
            buildConfigField("String", "APP_FLAVOR", "\"visit\"")
            matchingFallbacks += listOf("visit")
        }

        create("dev") {
            dimension = "envType"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"https://dev.api.visitapp.com\"")
            buildConfigField("String", "SIGNALR_URL", "\"https://dev-signalr.visitapp.com/hub\"")
            matchingFallbacks += listOf("dev")
        }
        create("stage") {
            dimension = "envType"
            applicationIdSuffix = ".stage"
            versionNameSuffix = "-stage"
            buildConfigField("String", "BASE_URL", "\"https://stage.api.visitapp.com\"")
            buildConfigField("String", "SIGNALR_URL", "\"https://stage-signalr.visitapp.com/hub\"")
            matchingFallbacks += listOf("stage")
        }
        create("prod") {
            dimension = "envType"
            buildConfigField("String", "BASE_URL", "\"https://api.visitapp.com\"")
            buildConfigField("String", "SIGNALR_URL", "\"https://signalr.visitapp.com/hub\"")
            matchingFallbacks += listOf("prod")
        }
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
    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // --- پایه اندروید/کامپوز/تست (از libs) ---
    implementation(libs.androidx.core.ktx)
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
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- ماژول‌ها ---
    implementation(project(":core-common"))
    implementation(project(":core-di"))
    implementation(project(":core-ui"))
    implementation(project(":persistence"))

    // --- Koin (از dependency) ---
    implementation(dependency.koin.androidx.compose)
    implementation(dependency.koin.test)
    testImplementation(dependency.koin.android.test)

    // --- Room ---
    implementation(dependency.room.runtime)
    implementation(dependency.room.ktx)
    ksp(dependency.room.compiler)        // لازم در app (به خاطر AppDatabase)

    // --- Timber ---
    implementation(dependency.timber.log)

    // --- Firebase (BOM) ---
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
}