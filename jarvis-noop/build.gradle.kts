plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.library.maven.publish)
}

android {
    namespace = "com.jarvis.library.noop"
    
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    // Minimal dependencies for API compatibility only
    compileOnly(libs.androidx.core.ktx)
    compileOnly(libs.androidx.activity.compose)
}