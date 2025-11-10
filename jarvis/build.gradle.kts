import java.util.Properties
import org.gradle.api.publish.tasks.GenerateModuleMetadata

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
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.metalava)
    id("com.vanniktech.maven.publish")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Set manifest placeholders for Sentry configuration
        manifestPlaceholders["sentryDsn"] = securityProperties["SENTRY_DSN"] ?: "https://dummy-sentry-dsn-replace-with-actual@sentry.io/project-id"

        // Add SDK version to BuildConfig
        buildConfigField("String", "SDK_VERSION", "\"${libs.versions.jarvisVersion.get()}\"")

        // Add API URLs to BuildConfig
        buildConfigField("String", "RATING_API_BASE_URL", "\"https://porfolio-keystone-server-production.up.railway.app/api/\"")
    }

    namespace = "com.jarvis.library"

    buildFeatures {
        buildConfig = true
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        warningsAsErrors = false
        checkReleaseBuilds = false
        ignoreWarnings = false
    }
}

// Configure Metalava for API tracking
metalava {
    // Source paths for API generation
    sourcePaths.setFrom("src/main/java")

    // Output API file
    filename.set("api/jarvis-api.txt")

    // Report lint issues as errors
    reportLintsAsErrors.set(false)

    // Include signature version info
    includeSignatureVersion.set(false)
}
// Configure Vanniktech Maven Publishing
mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = "io.github.jdumasleon",
        artifactId = "jarvis-android-sdk",
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
        name.set("Jarvis Android SDK")
        description.set("Android SDK for Jarvis network inspection and debugging toolkit")
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

val githubProperties: Properties = Properties().apply {
    val githubPropertiesFile = rootProject.file("github.properties")
    if (githubPropertiesFile.exists()) {
        load(githubPropertiesFile.inputStream())
    }
}

// Add GitHub Packages repository manually
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/jdumasleon/jarvis-sdk-android")
            credentials {
                username = githubProperties["gpr.usr"]?.toString() ?: System.getenv("GITHUB_ACTOR")
                password = githubProperties["gpr.key"]?.toString() ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// Disable Gradle metadata generation to prevent dependency resolution issues
tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

dependencies {
    // Use implementation to include internal modules in AAR
    api(projects.core)

    // Features that remain as separate modules
    api(projects.features.inspector)
    api(projects.features.preferences)

    // Dependencies from consolidated home and settings modules
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences.core)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.protobuf.kotlin.lite)

    implementation(libs.androidx.compose.material.iconsExtended)

    // Koin - Optional dependency for Koin integration
    // Apps using Koin can use the integration classes in com.jarvis.integration.koin package
    compileOnly(libs.koin.android)
    compileOnly(libs.koin.core)
    compileOnly(libs.koin.androidx.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation3.ui.android)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)


    // UI and lifecycle dependencies from home/settings presentation layers
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)

    // WorkManager for background tasks
    implementation(libs.androidx.work.ktx)
    implementation(libs.hilt.ext.work)

    // Network dependencies from settings data layer
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofitConverterGson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)

    api(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

