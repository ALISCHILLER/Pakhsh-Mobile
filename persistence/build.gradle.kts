plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(dependency.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(dependency.plugins.kotlin.serialization)
}

android {
    namespace = "com.msa.persistence"
    compileSdk = 35



    defaultConfig {
        minSdk = 27
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    flavorDimensions += listOf("appType", "envType")
    productFlavors {
        create("visit") {
            dimension = "appType"
            buildConfigField("String", "APP_FLAVOR", "\"visit\"")
            matchingFallbacks += listOf("visit")
        }
        create("supervisor") {
            dimension = "appType"
            buildConfigField("String", "APP_FLAVOR", "\"supervisor\"")
            matchingFallbacks += listOf("supervisor")
        }
        create("distribute") {
            dimension = "appType"
            buildConfigField("String", "APP_FLAVOR", "\"distribute\"")
            matchingFallbacks += listOf("distribute")
        }

        create("dev") {
            dimension = "envType"
            matchingFallbacks += listOf("dev")
        }
        create("stage") {
            dimension = "envType"
            matchingFallbacks += listOf("stage")
        }
        create("prod") {
            dimension = "envType"
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
    testImplementation(libs.junit)
    testImplementation(dependency.coroutines.test)
    testImplementation(dependency.androidx.test.core)
    testImplementation(dependency.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(project(":core-common"))
    implementation(project(":core-network"))
    implementation(project(":core-storage"))
    //DB Room
    implementation(dependency.room.runtime)
    ksp(dependency.room.compiler)
    implementation(dependency.room.ktx)

    //di koin
    implementation(dependency.koin.androidx.compose)
    implementation(dependency.koin.test)
    testImplementation(dependency.koin.android.test)

    // google gson
    implementation(dependency.google.gson)

    //network ktor
    implementation(platform(dependency.ktor.bom))
    implementation(dependency.ktor.android)
    implementation(dependency.ktor.logging)
    implementation(dependency.ktor.negotiation)
    implementation(dependency.ktor.json)
    implementation(dependency.ktor.okhttp)
    implementation(dependency.ktor.auth)
    implementation(dependency.ktor.core)
    implementation(dependency.ktor.resources)

    // signalR
    implementation ("com.microsoft.signalr:signalr:5.0.4")

    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation(dependency.timber.log)
}