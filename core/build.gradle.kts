plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.library.compose)
    alias(libs.plugins.jarvis.android.library.jacoco)
    alias(libs.plugins.jarvis.android.room)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.metalava)
}

android {
    namespace = "com.jarvis.core"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "POSTHOG_API_KEY", "\"phc_test_api_key\"")
        }
        getByName("release") {
            buildConfigField("String", "POSTHOG_API_KEY", "\"phc_test_api_key\"")
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        warningsAsErrors = false
        checkReleaseBuilds = false
        ignoreWarnings = false
        xmlReport = true
        htmlReport = true
        textReport = true
        absolutePaths = false
    }
}

// Configure Metalava for API tracking
metalava {
    // Source paths for API generation
    sourcePaths.setFrom("src/main/java")

    // Output API file
    filename.set("api/core-api.txt")

    // Report lint issues as errors
    reportLintsAsErrors.set(false)

    // Include signature version info
    includeSignatureVersion.set(false)
}

dependencies {
    // DataStore dependencies (from common)
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences.core)
    implementation(libs.protobuf.kotlin.lite)

    // Retrofit and JSON (from data)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofitConverterGson)
    implementation(libs.gson)

    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.navigationSuite)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.animation.graphics.android)

    // Navigation dependencies (from navigation and presentation)
    implementation(libs.androidx.savedstate)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines and serialization
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)

    // DataStore preferences (from platform)
    implementation(libs.androidx.dataStore.preferences)

    // Sentry dependencies
    implementation(platform(libs.sentry.bom))
    implementation(libs.sentry.android)
    implementation(libs.sentry.android.okhttp)
    implementation(libs.sentry.android.navigation)
    implementation(libs.sentry.compose)

    // PostHog dependencies
    implementation(libs.posthog.android)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.androidx.compose.ui.test)
    testImplementation(libs.androidx.compose.ui.testManifest)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
}

// Configure Maven Publishing
mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    coordinates(
        groupId = "io.github.jdumasleon",
        artifactId = "jarvis-android-sdk-core",
        version = libs.versions.jarvisVersion.get()
    )

    configure(
        com.vanniktech.maven.publish.AndroidSingleVariantLibrary(
            variant = "prodComposeRelease",
            sourcesJar = true,
            publishJavadocJar = true
        )
    )

    pom {
        name.set("Jarvis Android SDK Core")
        description.set("Core components for Jarvis Android SDK - networking, design system, and platform services")
        url.set("https://github.com/jdumasleon/jarvis-sdk-android")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("jdumasleon")
                name.set("Jean Dumas Leon")
                email.set("jdumasleon@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/jdumasleon/jarvis-sdk-android.git")
            developerConnection.set("scm:git:ssh://github.com/jdumasleon/jarvis-sdk-android.git")
            url.set("https://github.com/jdumasleon/jarvis-sdk-android")
        }
    }
}