plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
}

android {
    namespace = "com.jarvis.core.navigation"
}

dependencies {
    implementation(projects.core.designsystem)

    implementation(libs.androidx.savedstate)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}