plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.library.compose)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
}

android {
    namespace = "com.jarvis.features.settings.lib"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(projects.features.settings.domain)
    api(projects.features.settings.data)
    api(projects.features.settings.presentation)
    
    implementation(projects.core.common)
    implementation(projects.core.presentation)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation.compose)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}