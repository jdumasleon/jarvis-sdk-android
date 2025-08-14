plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
}

android {
    namespace = "com.jarvis.features.home.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(projects.core.data)

    implementation(projects.features.home.domain)
    implementation(projects.features.inspector.domain)
    implementation(projects.features.preferences.domain)
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.gson)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}