plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.room)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.jarvis.features.inspector.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(projects.features.inspector.domain)
    
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}