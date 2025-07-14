package com.jarvis.core.domain.usecase

import com.jarvis.core.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetEnvironmentListUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(environmentsList: List<Pair<String, String>>) {
        preferencesRepository.setEnvironmentsList(environmentsList)
    }
}