@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@file:OptIn(InternalSerializationApi::class)

package com.jarvis.features.inspector.internal.domain.entity

import androidx.annotation.RestrictTo

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Network interception rule similar to Android Studio Network Inspector
 * Defines how to intercept and modify network requests/responses
 */
@Serializable
data class NetworkRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isEnabled: Boolean = true,
    val origin: RuleOrigin,
    val mode: RuleMode,
    val requestModifications: RequestModifications? = null,
    val responseModifications: ResponseModifications? = null,
    val created: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * Rule execution mode
 */
@Serializable
enum class RuleMode {
    /**
     * Inspect mode: Intercepts the call and applies modifications, then continues with actual network request
     */
    INSPECT,
    
    /**
     * Mock mode: Intercepts the call and responds with mock data, never makes actual network request
     */
    MOCK
}

/**
 * Origin criteria for matching network requests
 */
@Serializable
data class RuleOrigin(
    val protocols: List<String> = emptyList(), // http, https, ws, wss
    val hostUrl: String? = null, // example.com, *.api.com, localhost
    val port: Int? = null, // 8080, 443, 80
    val path: String? = null, // /api/users, /api/users/*, /auth/**
    val query: String? = null, // key=value, has key "debug"
    val method: String? = null // GET, POST, PUT, DELETE, PATCH
) {
    
    /**
     * Check if this origin matches a given network transaction
     */
    fun matches(transaction: NetworkTransaction): Boolean {
        val url = transaction.request.url
        val method = transaction.request.method.name
        
        // Protocol matching
        if (protocols.isNotEmpty()) {
            val transactionProtocol = url.substringBefore("://").lowercase()
            if (!protocols.any { it.lowercase() == transactionProtocol }) {
                return false
            }
        }
        
        // Host URL matching (supports wildcards)
        hostUrl?.let { pattern ->
            val host = extractHost(url)
            if (!matchesHostPattern(host, pattern)) {
                return false
            }
        }
        
        // Port matching
        port?.let { expectedPort ->
            val transactionPort = extractPort(url)
            if (transactionPort != expectedPort) {
                return false
            }
        }
        
        // Path matching (supports wildcards)
        path?.let { pattern ->
            val transactionPath = extractPath(url)
            if (!matchesPathPattern(transactionPath, pattern)) {
                return false
            }
        }
        
        // Query matching
        query?.let { queryPattern ->
            val transactionQuery = extractQuery(url)
            if (!matchesQueryPattern(transactionQuery, queryPattern)) {
                return false
            }
        }
        
        // Method matching
        this.method?.let { expectedMethod ->
            if (!method.equals(expectedMethod, true)) {
                return false
            }
        }
        
        return true
    }
    
    private fun extractHost(url: String): String {
        return try {
            val withoutProtocol = url.substringAfter("://")
            val hostWithPort = withoutProtocol.substringBefore("/")
            hostWithPort.substringBefore(":")
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun extractPort(url: String): Int? {
        return try {
            val withoutProtocol = url.substringAfter("://")
            val hostWithPort = withoutProtocol.substringBefore("/")
            if (hostWithPort.contains(":")) {
                hostWithPort.substringAfter(":").toIntOrNull()
            } else {
                when {
                    url.startsWith("https://") -> 443
                    url.startsWith("http://") -> 80
                    else -> null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractPath(url: String): String {
        return try {
            val withoutProtocol = url.substringAfter("://")
            val pathWithQuery = withoutProtocol.substringAfter("/", "")
            if (pathWithQuery.isEmpty()) "/" else "/$pathWithQuery".substringBefore("?")
        } catch (e: Exception) {
            "/"
        }
    }
    
    private fun extractQuery(url: String): String {
        return url.substringAfter("?", "")
    }
    
    private fun matchesHostPattern(host: String, pattern: String): Boolean {
        return when {
            pattern == "*" -> true
            pattern.startsWith("*.") -> {
                val suffix = pattern.substring(2)
                host.endsWith(suffix)
            }
            pattern.endsWith(".*") -> {
                val prefix = pattern.substring(0, pattern.length - 2)
                host.startsWith(prefix)
            }
            else -> host.equals(pattern, ignoreCase = true)
        }
    }
    
    private fun matchesPathPattern(path: String, pattern: String): Boolean {
        return when {
            pattern == "*" -> true
            pattern.endsWith("/**") -> {
                val prefix = pattern.substring(0, pattern.length - 3)
                path.startsWith(prefix)
            }
            pattern.endsWith("/*") -> {
                val prefix = pattern.substring(0, pattern.length - 2)
                path.startsWith(prefix) && !path.substring(prefix.length).contains("/")
            }
            pattern.contains("*") -> {
                // Convert glob pattern to regex
                val regex = pattern.replace("*", ".*").toRegex()
                regex.matches(path)
            }
            else -> path.equals(pattern, ignoreCase = true)
        }
    }
    
    private fun matchesQueryPattern(query: String, pattern: String): Boolean {
        if (pattern.isEmpty()) return query.isEmpty()
        
        val queryParams = parseQueryString(query)
        val patternParams = parseQueryString(pattern)
        
        return patternParams.all { (key, value) ->
            queryParams[key] == value || (value == "*" && queryParams.containsKey(key))
        }
    }
    
    private fun parseQueryString(query: String): Map<String, String> {
        if (query.isEmpty()) return emptyMap()
        
        return query.split("&").mapNotNull { param: String ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) {
                Pair(parts[0], parts[1])
            } else {
                Pair(parts[0], "")
            }
        }.toMap()
    }
}

/**
 * Modifications to apply to requests
 */
@Serializable
data class RequestModifications(
    val addHeaders: Map<String, String> = emptyMap(),
    val modifyHeaders: Map<String, String> = emptyMap(),
    val removeHeaders: List<String> = emptyList(),
    val modifyBody: String? = null,
    val modifyUrl: String? = null,
    val modifyMethod: String? = null
)

/**
 * Modifications to apply to responses  
 */
@Serializable
data class ResponseModifications(
    val statusCode: Int? = null,
    val statusMessage: String? = null,
    val addHeaders: Map<String, String> = emptyMap(),
    val modifyHeaders: Map<String, String> = emptyMap(),
    val removeHeaders: List<String> = emptyList(),
    val modifyBody: String? = null,
    val delay: Long = 0 // Delay in milliseconds
)

/**
 * Result of applying a rule to a network transaction
 */
@Serializable
data class RuleApplicationResult(
    val ruleId: String,
    val ruleName: String,
    val mode: RuleMode,
    val applied: Boolean,
    val modificationsApplied: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)
