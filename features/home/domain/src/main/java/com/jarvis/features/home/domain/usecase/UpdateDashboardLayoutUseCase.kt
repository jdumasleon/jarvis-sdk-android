package com.jarvis.features.home.domain.usecase

import com.jarvis.features.home.domain.entity.DashboardLayout
import com.jarvis.features.home.domain.repository.DashboardRepository

/**
 * Use case for updating dashboard layout configuration (drag & drop, card visibility, etc.)
 */
class UpdateDashboardLayoutUseCase(
    private val repository: DashboardRepository
) {
    /**
     * Update dashboard layout configuration
     */
    suspend operator fun invoke(layout: DashboardLayout) {
        repository.updateDashboardLayout(layout)
    }
}