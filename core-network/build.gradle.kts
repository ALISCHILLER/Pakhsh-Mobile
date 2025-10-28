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
    api(project(":core-common"))
    implementation(dependency.coroutines.core)
    implementation(dependency.ktor.core)

    testImplementation(libs.junit)
    testImplementation(dependency.coroutines.test)
    testImplementation(dependency.ktor.client.mock)
}
