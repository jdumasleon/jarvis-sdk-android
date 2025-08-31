package com.jarvis.features.settings.domain.entity

/**
 * Contains application information for Settings screen
 */
data class AppInfo(
    val appName: String,
    val version: String,
    val buildNumber: String,
    val packageName: String
)

/**
 * Mock data for development and previews
 */
object AppInfoMock {
    val mockAppInfo = AppInfo(
        appName = "Jarvis SDK",
        version = "1.0.0",
        buildNumber = "1",
        packageName = "com.jarvis.sdk"
    )
}