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
include(":jarvis")
include(":jarvis-noop")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:common")
include(":core:presentation")
include(":core:navigation")
include(":features:inspector")
include(":features:inspector:domain")
include(":features:inspector:data")
include(":features:inspector:presentation")
include(":features:inspector:lib")
include(":features:preferences")
include(":features:preferences:domain")
include(":features:preferences:data")
include(":features:preferences:presentation")
include(":features:preferences:lib")
include(":features:home")
include(":features:home:domain")
include(":features:home:data")
include(":features:home:presentation")
include(":features:home:lib")
include(":features:settings")
include(":features:settings:domain")
include(":features:settings:data")
include(":features:settings:presentation")
include(":features:settings:lib")
include(":platform:api")
include(":platform:data")
include(":platform:lib")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    Jarvis requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}
