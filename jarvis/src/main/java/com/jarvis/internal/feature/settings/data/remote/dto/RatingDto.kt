package com.jarvis.internal.feature.settings.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.jarvis.internal.feature.settings.domain.entity.Rating
import com.jarvis.internal.feature.settings.domain.entity.RatingSubmissionResult

/**
 * Data transfer object for rating
 */
data class RatingDto(
    @SerializedName("stars")
    val stars: Int,
    @SerializedName("description")
    val description: String,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("version")
    val version: String? = null,
    @SerializedName("platform")
    val platform: String = "android",
    @SerializedName("sdk_version")
    val sdkVersion: String = "1.0.0"
)

/**
 * Data transfer object for rating submission response
 */
data class RatingSubmissionResponseDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("submission_id")
    val submissionId: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long? = null
)

/**
 * Extension to convert domain Rating to RatingDto
 */
fun Rating.toDto(): RatingDto = RatingDto(
    stars = stars,
    description = description,
    userId = userId,
    timestamp = timestamp,
    version = version
)

fun RatingSubmissionResponseDto.toDomain(): RatingSubmissionResult = RatingSubmissionResult(
    success = success,
    submissionId = submissionId
)
