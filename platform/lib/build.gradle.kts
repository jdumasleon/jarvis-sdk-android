plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
}

android {
    namespace = "com.jarvis.platform.lib"
}

dependencies {
    api(projects.platform.api)
    implementation(projects.platform.data)
    
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}