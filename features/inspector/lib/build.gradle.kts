plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.room)
    alias(libs.plugins.jarvis.hilt)
}

android {
    namespace = "com.jarvis.features.inspector.lib"
}

dependencies {
    implementation(projects.features.inspector.domain)
    implementation(projects.features.inspector.data)
    implementation(projects.features.inspector.presentation)

    implementation(libs.okhttp)
    implementation(libs.androidx.navigation3.ui.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}