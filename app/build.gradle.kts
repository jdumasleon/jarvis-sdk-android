import com.jarvis.buildlogic.extensions.JarvisBuildType

plugins {
    alias(libs.plugins.jarvis.android.application)
    alias(libs.plugins.jarvis.android.application.compose)
    alias(libs.plugins.jarvis.android.application.flavors)
    alias(libs.plugins.jarvis.android.application.jacoco)
    alias(libs.plugins.jarvis.hilt)
    id("com.google.android.gms.oss-licenses-plugin")
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf)
}

android {
    defaultConfig {
        applicationId = "com.jarvis.demo"
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()

        // Custom test runner to set up Hilt dependency graph
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
    namespace = "com.jarvis.demo"
}

dependencies {
    api(projects.core.common)
    api(projects.core.designsystem)
    api(projects.core.presentation)
    api(projects.jarvis)
    api(projects.features.inspector.lib)
    api(projects.features.preferences.lib)

    // Common dependencies for both flavors
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.androidx.window.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation3.ui.android)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3.android)
    
    // Compose-specific dependencies (only for compose flavor)
    "composeImplementation"(libs.androidx.material3)
    "composeImplementation"(libs.androidx.activity.compose)
    "composeImplementation"(libs.androidx.compose.material3)
    "composeImplementation"(libs.androidx.compose.material3.adaptive)
    "composeImplementation"(libs.androidx.compose.material3.adaptive.layout)
    "composeImplementation"(libs.androidx.compose.material3.adaptive.navigation)
    "composeImplementation"(libs.androidx.compose.material3.windowSizeClass)
    "composeImplementation"(libs.androidx.compose.runtime.tracing)
    "composeImplementation"(libs.androidx.hilt.navigation.compose)
    "composeImplementation"(libs.androidx.lifecycle.runtimeCompose)
    "composeImplementation"(libs.androidx.navigation.compose)
    
    // Classic Views dependencies (only for classic flavor)
    "classicImplementation"(libs.material)
    "classicImplementation"("androidx.appcompat:appcompat:1.7.1")
    "classicImplementation"("androidx.constraintlayout:constraintlayout:2.2.0")
    "classicImplementation"("androidx.recyclerview:recyclerview:1.3.2")
    "classicImplementation"("androidx.cardview:cardview:1.0.0")
    "classicImplementation"("androidx.viewpager2:viewpager2:1.1.0")
    "classicImplementation"("androidx.navigation:navigation-fragment-ktx:2.9.2")
    "classicImplementation"("androidx.navigation:navigation-ui-ktx:2.9.2")
    "classicImplementation"("androidx.drawerlayout:drawerlayout:1.2.0")
    "classicImplementation"("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(libs.androidx.dataStore)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.dataStore.preferences.core)
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)
    
    // Network dependencies for demo API calls
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofitConverterGson)
    implementation(libs.okhttp.logging.interceptor)

    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.testManifest)

    kspTest(libs.hilt.compiler)

    testImplementation(libs.hilt.android.testing)

    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)
}

baselineProfile {
    // Don't build on every iteration of a full assemble.
    // Instead enable generation directly for the release build variant.
    automaticGenerationDuringBuild = false

    // Make use of Dex Layout Optimizations via Startup Profiles
    dexLayoutOptimization = true
}

dependencyGuard {
    configuration("prodComposeReleaseRuntimeClasspath")
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
