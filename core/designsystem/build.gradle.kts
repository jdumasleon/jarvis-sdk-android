plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.jarvis.android.library.compose)
    alias(libs.plugins.jarvis.android.library.jacoco)
    alias(libs.plugins.roborazzi)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    namespace = "com.jarvis.core.designsystem"
}


dependencies {

    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.adaptive)
    api(libs.androidx.compose.material3.navigationSuite)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.util)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.animation.graphics.android)

    testImplementation(libs.androidx.compose.ui.test)
    testImplementation(libs.androidx.compose.ui.testManifest)

    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
}