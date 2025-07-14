import java.util.Properties

val githubProperties: Properties = Properties().apply {
    val bitbucketPropertiesFile = rootProject.file("bitbucket.properties")
    if (bitbucketPropertiesFile.exists()) {
        load(bitbucketPropertiesFile.inputStream())
    }
}

plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.library.compose)
    alias(libs.plugins.jarvis.android.library.jacoco)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    namespace = "com.jarvis.library"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
}