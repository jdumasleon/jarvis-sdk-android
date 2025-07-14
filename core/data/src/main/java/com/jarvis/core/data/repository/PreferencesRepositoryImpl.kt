package com.jarvis.core.data.repository

import com.jarvis.core.data.source.local.PreferencesDataSource
import com.jarvis.core.domain.repository.PreferencesRepository
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource
) : PreferencesRepository {

    override suspend fun setIsForcingFailRequests(isForcingFail: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun setEnvironmentsList(environmentsList: List<Pair<String, String>>) {
        TODO("Not yet implemented")
    }

    override suspend fun setFirstEnvironment(firstEnvironment: Pair<String, String>) {
        TODO("Not yet implemented")
    }

}