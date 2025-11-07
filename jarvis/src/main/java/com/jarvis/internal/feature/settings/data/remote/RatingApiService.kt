package com.jarvis.internal.feature.settings.data.remote

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.data.graphql.GraphQLRequest
import com.jarvis.core.internal.data.graphql.GraphQLResponse
import com.jarvis.internal.feature.settings.data.remote.dto.RatingMutationVariables
import com.jarvis.internal.feature.settings.data.remote.dto.SubmitRatingData
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API service for rating operations using GraphQL
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface RatingApiService {
    @POST("graphql")
    suspend fun submitRating(
        @Body request: GraphQLRequest<RatingMutationVariables>
    ): GraphQLResponse<SubmitRatingData>
}