package com.jarvis.core.domain.repository

interface PreferencesRepository {
    suspend fun setIsForcingFailRequests(isForcingFail: Boolean)
    suspend fun setEnvironmentsList(environmentsList: List<Pair<String, String>>)
    suspend fun setFirstEnvironment(firstEnvironment: Pair<String, String>)
}