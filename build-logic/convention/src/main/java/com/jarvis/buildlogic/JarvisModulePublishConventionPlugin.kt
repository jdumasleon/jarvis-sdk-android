package com.jarvis.buildlogic

import com.jarvis.buildlogic.extensions.libs
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.util.Base64

/**
 * Plugin de convención para publicar módulos individuales del SDK Jarvis
 * Cada módulo tendrá su propio artifactId único basado en su ruta de proyecto
 */
class JarvisModulePublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.vanniktech.maven.publish")
            }

            // Configurar publicación después de que el proyecto sea evaluado
            afterEvaluate {
                configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
                    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
                    signAllPublications()

                    // Generar artifactId basado en la ruta del proyecto
                    val artifactId = generateArtifactId(project)

                    coordinates(
                        groupId = "io.github.jdumasleon",
                        artifactId = artifactId,
                        version = libs.findVersion("jarvisVersion").get().toString()
                    )

                    configure(
                        com.vanniktech.maven.publish.AndroidSingleVariantLibrary(
                            variant = "prodComposeRelease",
                            sourcesJar = true,
                            publishJavadocJar = true
                        )
                    )

                    pom {
                        name.set(generateModuleName(project))
                        description.set(generateModuleDescription(project))
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
            }
        }
    }

    private fun generateArtifactId(project: Project): String {
        val projectPath = project.path.removePrefix(":")
        return when {
            projectPath.isEmpty() -> "jarvis-android-sdk" // Root project
            projectPath == "jarvis" -> "jarvis-android-sdk"
            projectPath == "jarvis-noop" -> "jarvis-android-sdk-noop"
            else -> "jarvis-android-sdk-${projectPath.replace(":", "-")}"
        }
    }

    private fun generateModuleName(project: Project): String {
        val projectPath = project.path.removePrefix(":")
        return when {
            projectPath.isEmpty() -> "Jarvis Android SDK"
            projectPath == "jarvis" -> "Jarvis Android SDK"
            projectPath == "jarvis-noop" -> "Jarvis Android SDK (No-Op)"
            projectPath.startsWith("core:") -> "Jarvis SDK - ${projectPath.substringAfter("core:").replaceFirstChar { it.uppercase() }} Core"
            projectPath.startsWith("features:") -> {
                val featurePath = projectPath.substringAfter("features:")
                val parts = featurePath.split(":")
                "Jarvis SDK - ${parts.joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }} Feature"
            }
            projectPath.startsWith("platform:") -> "Jarvis SDK - ${projectPath.substringAfter("platform:").replaceFirstChar { it.uppercase() }} Platform"
            else -> "Jarvis SDK - ${projectPath.replace(":", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }}"
        }
    }

    private fun generateModuleDescription(project: Project): String {
        val projectPath = project.path.removePrefix(":")
        return when {
            projectPath.isEmpty() -> "Android SDK for Jarvis network inspection and debugging toolkit"
            projectPath == "jarvis" -> "Android SDK for Jarvis network inspection and debugging toolkit"
            projectPath == "jarvis-noop" -> "No-op version of Jarvis Android SDK for release builds - provides same API with zero overhead"
            projectPath == "core:common" -> "Common utilities and base classes for Jarvis SDK"
            projectPath == "core:data" -> "Data layer with repositories and data sources for Jarvis SDK"
            projectPath == "core:designsystem" -> "Design system and UI components for Jarvis SDK"
            projectPath == "core:domain" -> "Domain layer with use cases and business logic for Jarvis SDK"
            projectPath == "core:navigation" -> "Navigation utilities and routing for Jarvis SDK"
            projectPath == "core:presentation" -> "Presentation layer utilities for Jarvis SDK"
            projectPath == "features:home:lib" -> "Home feature module for Jarvis SDK"
            projectPath == "features:inspector:lib" -> "Network inspector feature module for Jarvis SDK"
            projectPath == "features:preferences:lib" -> "Preferences management feature module for Jarvis SDK"
            projectPath == "features:settings:lib" -> "Settings feature module for Jarvis SDK"
            projectPath == "platform:lib" -> "Platform services integration for Jarvis SDK"
            else -> "Module ${projectPath.replace(":", "/")} for Jarvis Android SDK"
        }
    }
}