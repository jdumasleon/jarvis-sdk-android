import java.util.Properties
import java.util.Base64
import com.vanniktech.maven.publish.SonatypeHost

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
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Set manifest placeholders for Sentry configuration
        manifestPlaceholders["sentryDsn"] = securityProperties["SENTRY_DSN"] ?: "https://dummy-sentry-dsn-replace-with-actual@sentry.io/project-id"
    }

    namespace = "com.jarvis.library"
    
    buildFeatures {
        buildConfig = true
    }
    
    buildTypes {
        getByName("debug") {
            // Debug builds get full functionality
            buildConfigField("boolean", "JARVIS_ENABLED", "true")
            buildConfigField("String", "JARVIS_VERSION", "\"${libs.versions.jarvisVersion.get()}\"")
        }
        getByName("release") {
            // Release builds get no-op functionality
            buildConfigField("boolean", "JARVIS_ENABLED", "false")
            buildConfigField("String", "JARVIS_VERSION", "\"${libs.versions.jarvisVersion.get()}\"")
        }
    }
}

// Configure repositories
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/jdumasleon/jarvis-sdk-android")
            credentials {
                username = githubProperties.getProperty("gpr.usr") ?: System.getenv("GITHUB_ACTOR") ?: "jdumasleon"
                password = githubProperties.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}

// Configure Vanniktech Maven Publish Plugin for Central Portal
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    
    // Configure signing for Central Portal with base64 key decoding
    signAllPublications()
    
    // Decode base64 PGP key if provided
    val base64Key = project.findProperty("signingInMemoryKey") as String?
    if (base64Key != null && base64Key.isNotEmpty()) {
        try {
            val decodedKey = String(Base64.getDecoder().decode(base64Key))
            if (decodedKey.contains("BEGIN PGP PRIVATE KEY")) {
                project.ext.set("signing.key", decodedKey)
                project.ext.set("signing.password", project.findProperty("signingInMemoryKeyPassword"))
            }
        } catch (e: Exception) {
            // If decoding fails, assume it's already in correct format
            project.ext.set("signing.key", base64Key)
            project.ext.set("signing.password", project.findProperty("signingInMemoryKeyPassword"))
        }
    }
    
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
    implementation(libs.androidx.navigation3.ui.android)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)

    api(libs.okhttp)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
}

