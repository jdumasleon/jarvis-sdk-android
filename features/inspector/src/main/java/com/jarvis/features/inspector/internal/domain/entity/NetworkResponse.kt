@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.inspector.internal.domain.entity

import androidx.annotation.RestrictTo

data class NetworkResponse(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val contentType: String? = null,
    val bodySize: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    val hasBody: Boolean
        get() = !body.isNullOrBlank()
        
    val isSuccessful: Boolean
        get() = statusCode in 200..299
        
    val isRedirect: Boolean
        get() = statusCode in 300..399
        
    val isClientError: Boolean
        get() = statusCode in 400..499
        
    val isServerError: Boolean
        get() = statusCode in 500..599
        
    val statusCategory: StatusCategory
        get() = when (statusCode) {
            in 100..199 -> StatusCategory.INFORMATIONAL
            in 200..299 -> StatusCategory.SUCCESS
            in 300..399 -> StatusCategory.REDIRECT
            in 400..499 -> StatusCategory.CLIENT_ERROR
            in 500..599 -> StatusCategory.SERVER_ERROR
            else -> StatusCategory.UNKNOWN
        }
        
    val isJson: Boolean
        get() = contentType?.contains("application/json") == true
        
    val isXml: Boolean
        get() = contentType?.contains("application/xml") == true || 
                contentType?.contains("text/xml") == true
                
    val isHtml: Boolean
        get() = contentType?.contains("text/html") == true
        
    val isImage: Boolean
        get() = contentType?.startsWith("image/") == true
        
    val isText: Boolean
        get() = contentType?.startsWith("text/") == true
}

enum class StatusCategory {
    INFORMATIONAL,
    SUCCESS,
    REDIRECT,
    CLIENT_ERROR,
    SERVER_ERROR,
    UNKNOWN
}