plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
}

android {
    namespace = "com.jarvis.features.settings.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(projects.core.data)

    implementation(projects.features.settings.domain)
    implementation(projects.features.preferences.domain)
    implementation(projects.features.inspector.domain)

    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofitConverterGson)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.gson)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}