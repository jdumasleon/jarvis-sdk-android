import java.util.Properties

val securityProperties: Properties = Properties().apply {
    val securityPropertiesFile = rootProject.file("security.properties")
    if (securityPropertiesFile.exists()) {
        load(securityPropertiesFile.inputStream())
    }
}

plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.hilt)
}

android {
    namespace = "com.jarvis.platform.data"
    
    defaultConfig {
        buildConfigField("String", "POSTHOG_API_KEY", "\"${securityProperties["POSTHOG_API_KEY"] ?: ""}\"")
        buildConfigField("String", "SENTRY_DSN", "\"${securityProperties["SENTRY_DSN"] ?: ""}\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.platform.api)
    
    // Sentry dependencies
    implementation(platform(libs.sentry.bom))
    implementation(libs.sentry.android)
    implementation(libs.sentry.android.okhttp)
    implementation(libs.sentry.android.navigation)
    implementation(libs.sentry.compose)
    
    // PostHog dependencies
    implementation(libs.posthog.android)
    
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.kotlinx.serialization.json)
}