plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.library.compose)
    alias(libs.plugins.jarvis.android.room)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
}

android {
    namespace = "com.jarvis.features.inspector.lib"
}

dependencies {
    api(projects.features.inspector.presentation)
    implementation(projects.core.presentation)
    implementation(projects.features.inspector.data)
    implementation(projects.features.inspector.domain)

    implementation(libs.okhttp)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}