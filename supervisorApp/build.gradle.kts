plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.msa.supervisorApp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.msa.supervisorApp"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        missingDimensionStrategy("appType", "supervisor")
        missingDimensionStrategy("envType", "prod")
    }
    flavorDimensions += listOf("appType", "envType")
    productFlavors {
        create("supervisor") {
            dimension = "appType"
            applicationId = "com.msa.supervisorApp"
            buildConfigField("String", "APP_FLAVOR", "\"supervisor\"")
            matchingFallbacks += listOf("supervisor")
        }

        create("dev") {
            dimension = "envType"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"https://dev.api.supervisorapp.com\"")
            buildConfigField("String", "SIGNALR_URL", "\"https://dev-signalr.supervisorapp.com/hub\"")
            matchingFallbacks += listOf("dev")
        }
        create("stage") {
            dimension = "envType"
            applicationIdSuffix = ".stage"
            versionNameSuffix = "-stage"
            buildConfigField("String", "BASE_URL", "\"https://stage.api.supervisorapp.com\"")
            buildConfigField("String", "SIGNALR_URL", "\"https://stage-signalr.supervisorapp.com/hub\"")
            matchingFallbacks += listOf("stage")
        }
        create("prod") {
            dimension = "envType"
            buildConfigField("String", "BASE_URL", "\"https://api.supervisorapp.com\"")
            buildConfigField("String", "SIGNALR_URL", "\"https://signalr.supervisorapp.com/hub\"")
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
        buildConfig = true // ⬅️ این خط مهمه
    }
}

dependencies {

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
    implementation(project(":core-common"))
    implementation(project(":core-di"))
    implementation(project(":core-ui"))
    implementation(project(":persistence"))

    //di koin
    implementation(dependency.koin.androidx.compose)
    implementation(dependency.koin.test)
    testImplementation(dependency.koin.android.test)
    implementation(dependency.timber.log)
}