plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.jarvis.feature.inspector.domain"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}