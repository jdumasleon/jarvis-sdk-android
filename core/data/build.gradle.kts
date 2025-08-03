plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
    alias(libs.plugins.jarvis.android.room)
}

android {
    namespace = "com.jarvis.core.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(projects.core.common)
    api(projects.core.domain)

    // Retrofit
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofitConverterGson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}