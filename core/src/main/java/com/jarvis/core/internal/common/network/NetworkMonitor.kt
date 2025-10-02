package com.jarvis.core.internal.common.network

import androidx.annotation.RestrictTo

import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}