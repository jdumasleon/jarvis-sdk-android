import com.vanniktech.maven.publish.SonatypeHost
import java.util.Properties

plugins {
    alias(libs.plugins.jarvis.android.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.jarvis.library.noop"
    
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

// Load publishing properties
val securityProperties = Properties().apply {
    val securityPropertiesFile = rootProject.file("security.properties")
    if (securityPropertiesFile.exists()) {
        load(securityPropertiesFile.inputStream())
    }
}

// Configure Vanniktech Maven Publish Plugin for Central Portal
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    
    // Configure signing for Central Portal
    signAllPublications()
    
    coordinates(
        groupId = "io.github.jdumasleon",
        artifactId = "jarvis-android-sdk-noop", 
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
        name.set("Jarvis Android SDK (No-Op)")
        description.set("No-op version of Jarvis Android SDK for release builds - provides same API with zero overhead")
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
    // Minimal dependencies for API compatibility only
    compileOnly(libs.androidx.core.ktx)
    compileOnly(libs.androidx.activity.compose)
}