package com.jarvis.buildlogic

import com.jarvis.buildlogic.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import java.util.Properties

class AndroidLibraryMavenPublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("maven-publish")
            }

            val githubProperties: Properties = Properties().apply {
                val githubPropertiesFile = project.rootProject.file("github.properties")
                if (githubPropertiesFile.exists()) {
                    load(githubPropertiesFile.inputStream())
                }
            }

            extensions.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("debug") {
                        groupId = "com.jarvis"
                        artifactId = "jarvis-mode-debug"
                        version = libs.findVersion("jarvisVersion").get().toString()
                        artifact("${project.buildDir}/outputs/aar/jarvis-mode-debug.aar")
                    }
                    create<MavenPublication>("release") {
                        groupId = "com.jarvis"
                        artifactId = "jarvis-mode-release"
                        version = libs.findVersion("jarvisVersion").get().toString()
                        artifact("${project.buildDir}/outputs/aar/jarvis-mode-release.aar")
                    }
                }

                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = project.uri("https://maven.pkg.github.com/jdumasleon/jarvis-android")
                        credentials {
                            username = githubProperties["gpr.usr"].toString()
                            password = githubProperties["gpr.key"].toString()
                        }
                    }
                }
            }
        }
    }
}