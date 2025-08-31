plugins {
    alias(libs.plugins.jarvis.android.library)
}

android {
    namespace = "com.jarvis.platform.api"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}