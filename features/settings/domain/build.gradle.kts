plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
}

android {
    namespace = "com.jarvis.features.settings.domain"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}