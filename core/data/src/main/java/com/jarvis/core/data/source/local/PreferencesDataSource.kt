package com.jarvis.core.data.source.local

interface PreferencesDataSource {
    suspend fun setIsForcingFailRequests(isForcingFail: Boolean)
    suspend fun setEnvironmentsList(environmentsList: List<Pair<String, String>>)
    suspend fun setFirstEnvironment(firstEnvironment: Pair<String, String>)
}