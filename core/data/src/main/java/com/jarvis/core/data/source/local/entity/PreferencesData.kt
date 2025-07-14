package com.hiberus.jarvis.core.data.source.local.entity

/**
 * Class summarizing preferences interest data
 */
data class PreferencesData(
    val isForcingFailRequests: Boolean,
    val environmentsList: Set<String>,
    val firstEnvironment: Set<String>
)
