plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
}

android {
    namespace = "com.jarvis.features.preferences.lib"
}

dependencies {
    implementation(projects.core.presentation)
    implementation(projects.features.preferences.domain)
    implementation(projects.features.preferences.data)
    implementation(projects.features.preferences.presentation)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}