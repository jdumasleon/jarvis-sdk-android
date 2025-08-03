package com.jarvis.features.inspector.data.network

import com.jarvis.features.inspector.domain.entity.HttpMethod
import com.jarvis.features.inspector.domain.entity.NetworkRequest
import com.jarvis.features.inspector.domain.entity.NetworkResponse
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JarvisNetworkInterceptor @Inject constructor(
    private val collector: JarvisNetworkCollector
) : Interceptor {
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val MAX_CONTENT_LENGTH = 250_000L
        private val DEFAULT_REDACTED_HEADERS = setOf(
            "authorization",
            "cookie",
            "set-cookie",
            "x-api-key",
            "x-auth-token",
            "authentication"
        )
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        // Create NetworkRequest
        val networkRequest = createNetworkRequest(request, startTime)
        
        // Create initial transaction
        val transaction = NetworkTransaction(
            request = networkRequest,
            startTime = startTime
        )
        
        // Collect request
        coroutineScope.launch {
            collector.onRequestSent(transaction)
        }
        
        return try {
            val response = chain.proceed(request)
            val endTime = System.currentTimeMillis()
            
            // Create NetworkResponse
            val networkResponse = createNetworkResponse(response, endTime)
            
            // Update transaction with response
            val completedTransaction = transaction.withResponse(networkResponse)
            
            // Collect response
            coroutineScope.launch {
                collector.onResponseReceived(completedTransaction)
            }
            
            response
        } catch (e: Exception) {
            val errorTransaction = transaction.withError(e.message ?: "Unknown error")
            
            // Collect error
            coroutineScope.launch {
                collector.onFailure(errorTransaction, e)
            }
            
            throw e
        }
    }
    
    private fun createNetworkRequest(request: okhttp3.Request, startTime: Long): NetworkRequest {
        val headers = request.headers.toMultimap().mapValues { it.value.firstOrNull() ?: "" }
        val redactedHeaders = redactHeaders(headers)
        
        val body = try {
            request.body?.let { requestBody ->
                if (requestBody.contentLength() <= MAX_CONTENT_LENGTH) {
                    val buffer = Buffer()
                    requestBody.writeTo(buffer)
                    buffer.readUtf8()
                } else {
                    "[Content too large to display]"
                }
            }
        } catch (e: Exception) {
            "[Error reading request body: ${e.message}]"
        }
        
        return NetworkRequest(
            url = request.url.toString(),
            method = HttpMethod.fromString(request.method),
            headers = redactedHeaders,
            body = body,
            contentType = request.body?.contentType()?.toString(),
            bodySize = request.body?.contentLength() ?: 0L,
            timestamp = startTime
        )
    }
    
    private fun createNetworkResponse(response: Response, endTime: Long): NetworkResponse {
        val headers = response.headers.toMultimap().mapValues { it.value.firstOrNull() ?: "" }
        val redactedHeaders = redactHeaders(headers)
        
        val body = try {
            val responseBody = response.peekBody(MAX_CONTENT_LENGTH)
            responseBody.string()
        } catch (e: Exception) {
            "[Error reading response body: ${e.message}]"
        }
        
        return NetworkResponse(
            statusCode = response.code,
            statusMessage = response.message,
            headers = redactedHeaders,
            body = body,
            contentType = response.body?.contentType()?.toString(),
            bodySize = response.body?.contentLength() ?: 0L,
            timestamp = endTime
        )
    }
    
    private fun redactHeaders(headers: Map<String, String>): Map<String, String> {
        return headers.mapValues { (key, value) ->
            if (DEFAULT_REDACTED_HEADERS.contains(key.lowercase())) {
                "██"
            } else {
                value
            }
        }
    }
    
    class Builder {
        private var collector: JarvisNetworkCollector? = null
        private var maxContentLength: Long = MAX_CONTENT_LENGTH
        private var headersToRedact: Set<String> = DEFAULT_REDACTED_HEADERS
        
        fun collector(collector: JarvisNetworkCollector) = apply {
            this.collector = collector
        }
        
        fun maxContentLength(length: Long) = apply {
            this.maxContentLength = length
        }
        
        fun redactHeaders(vararg headers: String) = apply {
            this.headersToRedact = headers.map { it.lowercase() }.toSet()
        }
        
        fun build(): JarvisNetworkInterceptor {
            val networkCollector = collector ?: throw IllegalStateException("Collector must be set")
            return JarvisNetworkInterceptor(networkCollector)
        }
    }
}