package com.jarvis.features.settings.domain.entity

/**
 * Represents a user rating for the SDK
 */
data class Rating(
    val stars: Int,
    val description: String,
    val userId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val version: String? = null
) {
    init {
        require(stars in 1..5) { "Stars must be between 1 and 5" }
        require(description.isNotBlank()) { "Description cannot be blank" }
    }
}

/**
 * Response from submitting a rating
 */
data class RatingSubmissionResult(
    val success: Boolean,
    val submissionId: String? = null
)