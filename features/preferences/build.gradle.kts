plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.library.compose)
    alias(libs.plugins.jarvis.hilt)
    alias(libs.plugins.jarvis.android.library.jacoco)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.metalava)
}

android {
    namespace = "com.jarvis.features.preferences"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

// Configure Metalava for API tracking
metalava {
    // Source paths for API generation
    sourcePaths.setFrom("src/main/java")

    // Output API file
    filename.set("api/preferences-api.txt")

    // Report lint issues as errors
    reportLintsAsErrors.set(false)

    // Include signature version info
    includeSignatureVersion.set(false)
}

dependencies {
    // Core dependency
    api(projects.core)

    // AndroidX dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.dataStore)
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.dataStore.preferences.core)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.navigation3.ui.android)
    implementation(libs.androidx.navigation3.runtime)

    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)

    // Kotlin dependencies
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // External libraries
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.gson)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

// Configure Maven Publishing
mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    coordinates(
        groupId = "io.github.jdumasleon",
        artifactId = "jarvis-android-sdk-preferences",
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
        name.set("Jarvis Android SDK Preferences")
        description.set("Preferences management and configuration tools for Jarvis Android SDK")
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