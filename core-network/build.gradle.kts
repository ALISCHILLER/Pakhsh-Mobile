plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation(project(":core-common"))

    implementation(platform(dependency.ktor.bom))
    implementation(dependency.ktor.core)
    implementation(dependency.ktor.okhttp)
    implementation(dependency.ktor.logging)
    implementation(dependency.ktor.negotiation)
    implementation(dependency.ktor.json)

    implementation(dependency.kotlinx.serialization.json)
    implementation(dependency.coroutines.core)
}
