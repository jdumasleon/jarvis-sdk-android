plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
}

android {
    namespace = "com.jarvis.core.common"
}

dependencies {
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences.core)
    implementation(libs.protobuf.kotlin.lite)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
}