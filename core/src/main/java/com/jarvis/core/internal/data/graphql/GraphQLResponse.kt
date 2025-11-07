@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.data.graphql

import androidx.annotation.RestrictTo
import com.google.gson.annotations.SerializedName

/**
 * Generic GraphQL response wrapper
 */
data class GraphQLResponse<T>(
    @SerializedName("data")
    val data: T?,
    @SerializedName("errors")
    val errors: List<GraphQLError>? = null
)

/**
 * GraphQL error structure
 */
data class GraphQLError(
    @SerializedName("message")
    val message: String,
    @SerializedName("locations")
    val locations: List<ErrorLocation>? = null,
    @SerializedName("path")
    val path: List<String>? = null
)

/**
 * GraphQL error location
 */
data class ErrorLocation(
    @SerializedName("line")
    val line: Int,
    @SerializedName("column")
    val column: Int
)
