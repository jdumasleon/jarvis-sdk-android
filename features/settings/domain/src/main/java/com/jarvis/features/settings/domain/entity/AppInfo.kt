package com.jarvis.features.settings.domain.entity

/**
 * Contains application information for Settings screen
 * @deprecated Use SdkInfo and HostAppInfo instead for clearer separation
 */
data class AppInfo(
    val appName: String,
    val version: String,
    val buildNumber: String,
    val packageName: String
)

/**
 * Contains SDK-specific information
 */
data class SdkInfo(
    val name: String = "Jarvis SDK",
    val version: String,
    val buildNumber: String
)

/**
 * Contains host application information
 */
data class HostAppInfo(
    val appName: String,
    val version: String,
    val buildNumber: String,
    val packageName: String,
    val minSdkVersion: Int? = null,
    val targetSdkVersion: Int? = null,
    val permissions: List<String> = emptyList()
)

/**
 * Combined information for settings screen
 */
data class SettingsAppInfo(
    val sdkInfo: SdkInfo,
    val hostAppInfo: HostAppInfo
)

/**
 * Mock data for development and previews
 */
object AppInfoMock {
    val mockAppInfo = AppInfo(
        appName = "Jarvis Demo",
        version = "1.0.0",
        buildNumber = "1",
        packageName = "com.jarvis.demo"
    )

    val mockSdkInfo = SdkInfo(
        version = "1.0.28",
        buildNumber = "28"
    )

    val mockHostAppInfo = HostAppInfo(
        appName = "Jarvis Demo",
        version = "1.0.0",
        buildNumber = "1",
        packageName = "com.jarvis.demo",
        minSdkVersion = 24,
        targetSdkVersion = 34,
        permissions = listOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE"
        )
    )

    val mockSettingsAppInfo = SettingsAppInfo(
        sdkInfo = mockSdkInfo,
        hostAppInfo = mockHostAppInfo
    )
}