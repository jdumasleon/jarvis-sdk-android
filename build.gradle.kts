buildscript {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13617490/artifacts/repository")
        }

        // Android Build Server
        maven { url = uri("../jarvis-prebuilts/m2repository") }
    }
    dependencies {
        classpath(libs.google.oss.licenses.plugin) {
            // exclude(group = "com.google.protobuf")
        }
    }
}

// Lists all plugins used throughout the project
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dependencyGuard) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.module.graph) apply true
    alias(libs.plugins.jetbrains.kotlin.android) apply false // Plugin applied to allow module graph generation
    alias(libs.plugins.detekt) apply true
    alias(libs.plugins.ktlint) apply true
    alias(libs.plugins.binaryCompatibilityValidator) apply true
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

// Configure Binary Compatibility Validator for API tracking
apiValidation {
    // Only validate these modules (our SDK modules)
    validationDisabled = false

    // Track these modules for binary compatibility
    ignoredProjects += listOf("app") // Exclude demo app

    // Generate API files in each module
    apiDumpDirectory = "api"
}

