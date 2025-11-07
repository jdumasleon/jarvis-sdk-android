@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.settings.data.remote.dto

import androidx.annotation.RestrictTo
import com.google.gson.annotations.SerializedName
import com.jarvis.internal.feature.settings.domain.entity.Rating
import com.jarvis.internal.feature.settings.domain.entity.RatingSubmissionResult

/**
 * Variables for the submitRating mutation
 */
data class RatingMutationVariables(
    @SerializedName("data")
    val data: RatingSubmissionInput
)

/**
 * Input data for rating submission matching the GraphQL schema
 */
data class RatingSubmissionInput(
    @SerializedName("stars")
    val stars: Int,
    @SerializedName("description")
    val description: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("timestamp")
    val timestamp: String, // ISO 8601 format
    @SerializedName("version")
    val version: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("sdkVersion")
    val sdkVersion: String
)

/**
 * Data returned from submitRating mutation
 */
data class SubmitRatingData(
    @SerializedName("submitRating")
    val submitRating: SubmitRatingResponse
)

/**
 * Response from submitRating mutation
 */
data class SubmitRatingResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("submissionId")
    val submissionId: String?,
    @SerializedName("timestamp")
    val timestamp: String?
)

/**
 * GraphQL mutation query string
 */
object RatingMutations {
    const val SUBMIT_RATING = """
        mutation SubmitRating(${'$'}data: RatingSubmissionInput!) {
          submitRating(data: ${'$'}data) {
            success
            message
            submissionId
            timestamp
          }
        }
    """
}

/**
 * Extension to convert domain Rating to GraphQL input
 */
fun Rating.toGraphQLInput(): RatingSubmissionInput {
    return RatingSubmissionInput(
        stars = stars,
        description = description,
        userId = userId ?: "anonymous",
        timestamp = timestamp.toString(),
        version = version ?: "unknown",
        platform = "android",
        sdkVersion = version ?: "unknown"
    )
}

/**
 * Extension to convert GraphQL response to domain result
 */
fun SubmitRatingResponse.toDomain(): RatingSubmissionResult = RatingSubmissionResult(
    success = success,
    submissionId = submissionId
)
