import com.jarvis.buildlogic.extensions.JarvisBuildType
import org.gradle.kotlin.dsl.debugImplementation

plugins {
    alias(libs.plugins.jarvis.android.application)
    alias(libs.plugins.jarvis.android.application.compose)
    alias(libs.plugins.jarvis.android.application.flavors)
    // NO HILT - Pure Koin integration!
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.baselineprofile)
}

android {
    defaultConfig {
        applicationId = "com.jarvis.demo.koin"
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = JarvisBuildType.DEBUG.applicationIdSuffix
        }
        release {
            isMinifyEnabled = true
            applicationIdSuffix = JarvisBuildType.RELEASE.applicationIdSuffix
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            // To publish on the Play store a private signing key is required, but to allow anyone
            // who clones the code to sign and run the release variant, use the debug signing key.
            // TODO: Abstract the signing configuration to a separate file to avoid hardcoding this.
            signingConfig = signingConfigs.named("debug").get()
            // Ensure Baseline Profile is fresh for release builds.
            baselineProfile.automaticGenerationDuringBuild = true
        }
    }


    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    namespace = "com.jarvis.demo.koin"
    kotlinOptions {
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {
    // For demo purposes - using local project reference
    // In host applications, use: implementation("io.github.jdumasleon:jarvis-android-sdk:1.0.28")
    implementation(projects.jarvis)
    implementation(projects.features.inspector)
    implementation(projects.features.preferences)

    // Core modules are now accessible through the main jarvis module via API dependencies
    api(projects.core)

    // Common dependencies for both flavors
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.androidx.window.core)
    implementation(libs.kotlinx.serialization.json)


    "composeImplementation"(libs.androidx.navigation3.ui.android)
    "composeImplementation"(libs.androidx.lifecycle.viewmodel.compose)
    "composeImplementation"(libs.androidx.activity.compose)
    "composeImplementation"(libs.androidx.compose.material3)
    "composeImplementation"(libs.androidx.compose.material3.adaptive)
    "composeImplementation"(libs.androidx.compose.material3.adaptive.layout)
    "composeImplementation"(libs.androidx.compose.material3.adaptive.navigation)
    "composeImplementation"(libs.androidx.compose.material3.windowSizeClass)
    "composeImplementation"(libs.androidx.compose.runtime.tracing)
    "composeImplementation"(libs.androidx.lifecycle.runtimeCompose)
    "composeImplementation"(libs.androidx.navigation3.ui.android)

    implementation(libs.androidx.metrics)
    implementation(libs.androidx.dataStore)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.dataStore.preferences.core)
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)

    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofitConverterGson)
    implementation(libs.okhttp.logging.interceptor)

    debugImplementation(libs.androidx.compose.ui.testManifest)

    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)

    // Koin for dependency injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.core)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencyGuard {
    configuration("prodComposeReleaseRuntimeClasspath")
}

baselineProfile {
    // Don't build on every iteration of a full assemble.
    // Instead enable generation directly for the release build variant.
    automaticGenerationDuringBuild = false

    // Make use of Dex Layout Optimizations via Startup Profiles
    dexLayoutOptimization = true
}