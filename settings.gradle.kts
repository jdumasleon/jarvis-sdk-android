pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "JarvisDemo"

include(":app")
include(":jarvis")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:common")
include(":core:navigation")
include(":features:inspector")
