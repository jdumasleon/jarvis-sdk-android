@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.inspector.internal.domain.entity

import androidx.annotation.RestrictTo

data class NetworkRequest(
    val url: String,
    val method: HttpMethod,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val contentType: String? = null,
    val bodySize: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    val hasBody: Boolean
        get() = !body.isNullOrBlank()
        
    val isGraphQL: Boolean
        get() = contentType?.contains("application/json") == true && 
                (body?.contains("query") == true || body?.contains("mutation") == true)
                
    val protocol: String
        get() = if (url.startsWith("https://")) "HTTPS" else "HTTP"
        
    val host: String
        get() = try {
            url.substringAfter("://").substringBefore("/")
        } catch (e: Exception) {
            "Unknown"
        }
        
    val path: String
        get() = try {
            val withoutProtocol = url.substringAfter("://")
            val pathPart = withoutProtocol.substringAfter("/")
            if (pathPart == withoutProtocol) "/" else "/$pathPart"
        } catch (e: Exception) {
            "/"
        }
}

enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE, CONNECT;
    
    companion object {
        fun fromString(method: String): HttpMethod {
            return try {
                valueOf(method.uppercase())
            } catch (e: IllegalArgumentException) {
                GET
            }
        }
    }
}