import java.util.Properties
import java.util.Base64
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.publish.tasks.GenerateModuleMetadata

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

// Disable Gradle metadata generation to prevent dependency resolution issues
tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

// Configure Vanniktech Maven Publish Plugin for Central Portal
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    // Configure signing for Central Portal with base64 key decoding
    // signAllPublications() // Disabled for local testing
    
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

        withXml {
            val dependenciesNode = asNode().get("dependencies")
            if (dependenciesNode is List<*> && dependenciesNode.isNotEmpty()) {
                val deps = dependenciesNode.first()
                if (deps is groovy.util.Node) {
                    val toRemove = deps.children().filter { child ->
                        if (child is groovy.util.Node) {
                            val groupIdNodes = child.get("groupId")
                            if (groupIdNodes is List<*> && groupIdNodes.isNotEmpty()) {
                                val groupIdNode = groupIdNodes.first()
                                if (groupIdNode is groovy.util.Node) {
                                    val groupId = groupIdNode.text()
                                    // Remove internal project dependencies since they're included in the fat AAR
                                    groupId.startsWith("JarvisDemo") || groupId == project.group
                                } else false
                            } else false
                        } else false
                    }
                    toRemove.forEach { node ->
                        if (node is groovy.util.Node) {
                            deps.remove(node)
                        }
                    }
                }
            }
        }
    }
}

dependencies {
    // Use implementation to include internal modules in AAR
    implementation(projects.core)

    // Features that remain as separate modules
    implementation(projects.features.inspector)
    implementation(projects.features.preferences)

    // Dependencies from consolidated home and settings modules
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences.core)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.protobuf.kotlin.lite)

    implementation(libs.androidx.compose.material.iconsExtended)
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

