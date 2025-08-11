plugins {
    alias(libs.plugins.jarvis.jvm.library)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}