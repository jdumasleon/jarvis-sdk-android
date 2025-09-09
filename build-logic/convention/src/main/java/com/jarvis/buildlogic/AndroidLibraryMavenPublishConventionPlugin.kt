package com.jarvis.buildlogic

import com.android.build.gradle.LibraryExtension
import com.jarvis.buildlogic.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension
import java.util.Properties
import java.util.Base64

class AndroidLibraryMavenPublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("maven-publish")
                apply("signing")
            }

            val publishingProperties: Properties = Properties().apply {
                val publishingPropertiesFile = project.rootProject.file("publishing.properties")
                if (publishingPropertiesFile.exists()) {
                    load(publishingPropertiesFile.inputStream())
                }
            }

            val githubProperties: Properties = Properties().apply {
                val githubPropertiesFile = project.rootProject.file("github.properties")
                if (githubPropertiesFile.exists()) {
                    load(githubPropertiesFile.inputStream())
                }
            }

            extensions.configure<LibraryExtension> {
                publishing {
                    singleVariant("prodComposeRelease") {
                        withSourcesJar()
                        withJavadocJar()
                    }
                }
            }

            afterEvaluate {
                extensions.configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("release") {
                            groupId = "io.github.jdumasleon"
                            artifactId = if (project.name == "jarvis-noop") {
                                "jarvis-android-sdk-noop"
                            } else {
                                "jarvis-android-sdk"
                            }
                            version = libs.findVersion("jarvisVersion").get().toString()
                            
                            from(components["prodComposeRelease"])

                            pom {
                                name.set(if (project.name == "jarvis-noop") {
                                    "Jarvis Android SDK (No-Op)"
                                } else {
                                    "Jarvis Android SDK"
                                })
                                description.set(if (project.name == "jarvis-noop") {
                                    "No-op version of Jarvis Android SDK for release builds - provides same API with zero overhead"
                                } else {
                                    "Android SDK for Jarvis network inspection and debugging toolkit"
                                })
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

                    repositories {
                        // Maven Central via Sonatype OSSRH
                        maven {
                            name = "CentralPortalOSSRH"
                            val isSnapshot = version.toString().endsWith("SNAPSHOT")
                            url = if (isSnapshot) {
                                uri("https://central.sonatype.com/repository/maven-snapshots/")
                            } else {
                                uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                            }
                            credentials {
                                username = publishingProperties["ossrh.username"].toString().takeIf { it != "null" }
                                    ?: System.getenv("OSSRH_USERNAME")
                                password = publishingProperties["ossrh.password"].toString().takeIf { it != "null" }
                                    ?: System.getenv("OSSRH_PASSWORD")
                            }
                        }

                        // GitHub Packages (backup repository)
                        maven {
                            name = "GitHubPackages"
                            url = project.uri("https://maven.pkg.github.com/jdumasleon/jarvis-sdk-android")
                            credentials {
                                username = githubProperties["gpr.usr"].toString().takeIf { it != "null" }
                                    ?: System.getenv("GITHUB_ACTOR")
                                password = githubProperties["gpr.key"].toString().takeIf { it != "null" }
                                    ?: System.getenv("GITHUB_TOKEN")
                            }
                        }

                        // Local repository for testing
                        maven {
                            name = "Local"
                            url = uri("${project.layout.buildDirectory}/repo")
                        }
                    }
                }

                extensions.configure<SigningExtension> {
                    val signingKey = publishingProperties["signing.key"].toString().takeIf { it != "null" }
                        ?: System.getenv("SIGNING_KEY")
                    val signingPassword = publishingProperties["signing.password"].toString().takeIf { it != "null" }
                        ?: System.getenv("SIGNING_PASSWORD")

                    if (signingKey != null && signingPassword != null && !signingKey.contains("BEGIN PGP PRIVATE KEY BLOCK")) {
                        try {
                            useInMemoryPgpKeys(signingKey, signingPassword)
                            sign(extensions.getByType(PublishingExtension::class.java).publications)
                        } catch (e: Exception) {
                            println("Warning: Could not configure signing: ${e.message}")
                            // Skip signing if there are issues with the key
                        }
                    }
                }
            }
        }
    }
}