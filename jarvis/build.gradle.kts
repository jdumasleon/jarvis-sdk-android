import java.util.Properties

val githubProperties: Properties = Properties().apply {
    val bitbucketPropertiesFile = rootProject.file("bitbucket.properties")
    if (bitbucketPropertiesFile.exists()) {
        load(bitbucketPropertiesFile.inputStream())
    }
}

val securityProperties: Properties = Properties().apply {
    val securityPropertiesFile = rootProject.file("security.properties")
    if (securityPropertiesFile.exists()) {
        load(securityPropertiesFile.inputStream())
    }
}

plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.library.compose)
    alias(libs.plugins.jarvis.android.library.jacoco)
    alias(libs.plugins.jarvis.android.library.maven.publish)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Set manifest placeholders for Sentry configuration
        manifestPlaceholders["sentryDsn"] = securityProperties["SENTRY_DSN"] ?: "https://dummy-sentry-dsn-replace-with-actual@sentry.io/project-id"
    }

    namespace = "com.jarvis.library"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.presentation)

    implementation(projects.features.home.lib)
    implementation(projects.features.inspector.lib)
    implementation(projects.features.preferences.lib)
    implementation(projects.features.settings.lib)
    implementation(projects.features.preferences.domain)

    implementation(projects.platform.lib)
    implementation(projects.core.navigation)
    
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences.core)
    implementation(libs.protobuf.kotlin.lite)
    
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

