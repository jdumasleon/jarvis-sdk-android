package com.jarvis.core.domain.usecase

import com.jarvis.core.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetFirstEnvironmentUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(firstEnvironment: Pair<String, String>) {
        preferencesRepository.setFirstEnvironment(firstEnvironment)
    }
}