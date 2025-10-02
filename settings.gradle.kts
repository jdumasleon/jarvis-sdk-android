pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13617490/artifacts/repository")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal() // For testing locally published SDK
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13617490/artifacts/repository")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "JarvisDemo"

include(":app")
include(":app-koin")
include(":jarvis")
include(":core")
include(":features:inspector")
include(":features:preferences")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    Jarvis requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}
