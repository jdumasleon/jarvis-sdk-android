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
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.presentation)

    implementation(projects.features.inspector.lib)
    implementation(projects.features.preferences.lib)
    
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
}