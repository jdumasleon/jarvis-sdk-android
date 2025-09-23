@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.domain.preferences.usecase

import androidx.annotation.RestrictTo
import com.jarvis.core.internal.domain.preferences.repository.JarvisInternalPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for managing HeaderContent DSFlag state
 * This encapsulates the business logic for header content visibility and dismissal behavior.
 */
@Singleton
class ManageHeaderContentStateUseCase @Inject constructor(
    private val internalPreferencesRepository: JarvisInternalPreferencesRepository
) {

    /**
     * Get the current header content visibility state
     * @return Flow<Boolean> true if header should be shown, false if dismissed
     */
    fun isHeaderContentVisible(): Flow<Boolean> =
        internalPreferencesRepository.preferences.map { prefs ->
            !prefs.headerContentDismissed
        }

    /**
     * Dismiss the header content (user clicked close/dismiss)
     * This sets the flag and records the timestamp
     */
    suspend fun dismissHeaderContent() {
        internalPreferencesRepository.setHeaderContentDismissed(true)
    }

    /**
     * Reset header content visibility (for testing or new features)
     */
    suspend fun resetHeaderContentVisibility() {
        internalPreferencesRepository.setHeaderContentDismissed(false)
    }

    /**
     * Check if header content was dismissed and when
     * @return Flow<Pair<Boolean, Long>> - (isDismissed, dismissalTimestamp)
     */
    fun getHeaderContentDismissalInfo(): Flow<Pair<Boolean, Long>> =
        internalPreferencesRepository.preferences.map { prefs ->
            Pair(prefs.headerContentDismissed, prefs.lastHeaderDismissalTimestamp)
        }

    /**
     * Check if header should be shown based on business rules
     * For example: show again after certain time period, or on new app versions
     */
    fun shouldShowHeaderContent(): Flow<Boolean> =
        internalPreferencesRepository.preferences.map { prefs ->
            when {
                // Never been dismissed - show it
                !prefs.headerContentDismissed -> true

                // Dismissed but might show again based on business rules
                // For now, once dismissed = permanently hidden
                // Future: could add logic like "show again after X days" or "show for new features"
                else -> false
            }
        }
}