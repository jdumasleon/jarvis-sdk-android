package com.jarvis.features.inspector.internal.data.network

import androidx.annotation.RestrictTo

import com.jarvis.features.inspector.internal.domain.entity.HttpMethod
import com.jarvis.features.inspector.internal.domain.entity.NetworkRequest
import com.jarvis.features.inspector.internal.domain.entity.NetworkResponse
import com.jarvis.features.inspector.internal.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.internal.domain.entity.RuleMode
import com.jarvis.features.inspector.internal.domain.usecase.rules.ApplyNetworkRulesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class JarvisNetworkInterceptor @Inject constructor(
    private val collector: JarvisNetworkCollector,
    private val applyNetworkRulesUseCase: ApplyNetworkRulesUseCase
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
        val originalRequest = chain.request()
        val startTime = System.currentTimeMillis()
        
        // Create NetworkRequest
        val networkRequest = createNetworkRequest(originalRequest, startTime)
        
        // Create transaction for rule matching
        val transaction = NetworkTransaction(
            request = networkRequest,
            startTime = startTime
        )
        
        // Find matching rules
        val matchingRules = runBlocking {
            try {
                applyNetworkRulesUseCase.findMatchingRules(transaction)
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        // Get the first matching rule (if any)
        val firstRule = matchingRules.firstOrNull()
        
        // Check if this request should be mocked (Mock mode)
        if (firstRule?.mode == RuleMode.MOCK) {
            val mockNetworkResponse = applyNetworkRulesUseCase.createMockResponse(networkRequest, firstRule)
            val mockResponse = createMockOkHttpResponse(originalRequest, mockNetworkResponse)
            val finalNetworkResponse = createNetworkResponse(mockResponse, System.currentTimeMillis())
            
            // Create transaction with mock response
            val mockTransaction = transaction.withResponse(finalNetworkResponse)
            
            // Collect mock transaction
            coroutineScope.launch {
                collector.onRequestSent(mockTransaction)
                collector.onResponseReceived(mockTransaction)
            }
            
            return mockResponse
        }
        
        // Apply request modifications if in Inspect mode
        val modifiedNetworkRequest = if (firstRule?.mode == RuleMode.INSPECT) {
            applyNetworkRulesUseCase.applyRequestModifications(networkRequest, firstRule)
        } else {
            networkRequest
        }
        
        // Use modified request
        val requestToSend = if (modifiedNetworkRequest != networkRequest) {
            buildModifiedOkHttpRequest(originalRequest, modifiedNetworkRequest)
        } else {
            originalRequest
        }
        
        // Update transaction with potentially modified request
        val updatedTransaction = transaction.copy(request = modifiedNetworkRequest)
        
        // Collect request asynchronously with fire-and-forget approach
        coroutineScope.launch(Dispatchers.IO) {
            try {
                collector.onRequestSent(updatedTransaction)
            } catch (e: Exception) {
                // Log but don't block the network request
                println("JarvisNetworkInterceptor: Failed to collect request - ${e.message}")
            }
        }
        
        return try {
            val response = chain.proceed(requestToSend)
            val endTime = System.currentTimeMillis()
            
            // Create NetworkResponse
            val networkResponse = createNetworkResponse(response, endTime)
            
            // Apply response modifications if in Inspect mode
            val finalNetworkResponse = if (firstRule?.mode == RuleMode.INSPECT) {
                applyNetworkRulesUseCase.applyResponseModifications(networkResponse, firstRule)
            } else {
                networkResponse
            }
            
            val finalResponse = if (finalNetworkResponse != networkResponse) {
                createOkHttpResponseFromNetworkResponse(response, finalNetworkResponse)
            } else {
                response
            }
            
            // Update transaction with response
            val completedTransaction = updatedTransaction.withResponse(finalNetworkResponse)
            
            // Collect response asynchronously with fire-and-forget approach
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    collector.onResponseReceived(completedTransaction)
                } catch (e: Exception) {
                    // Log but don't block the network request
                    println("JarvisNetworkInterceptor: Failed to collect response - ${e.message}")
                }
            }
            
            finalResponse
        } catch (e: Exception) {
            val errorTransaction = updatedTransaction.withError(e.message ?: "Unknown error")
            
            // Collect error asynchronously with fire-and-forget approach
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    collector.onFailure(errorTransaction, e)
                } catch (ex: Exception) {
                    // Log but don't block the network request
                    println("JarvisNetworkInterceptor: Failed to collect error - ${ex.message}")
                }
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
            contentType = response.body.contentType()?.toString(),
            bodySize = response.body.contentLength() ?: 0L,
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
    
    private fun createMockOkHttpResponse(
        originalRequest: Request,
        networkResponse: NetworkResponse
    ): Response {
        val builder = Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(networkResponse.statusCode)
            .message(networkResponse.statusMessage)
        
        // Add headers
        networkResponse.headers.forEach { (key, value) ->
            builder.addHeader(key, value)
        }
        
        // Set response body
        val responseBody = networkResponse.body ?: ""
        val mediaType = networkResponse.contentType?.toMediaType()
        builder.body(responseBody.toResponseBody(mediaType))
        
        return builder.build()
    }
    
    private fun buildModifiedOkHttpRequest(
        originalRequest: Request,
        modifiedNetworkRequest: NetworkRequest
    ): Request {
        val builder = originalRequest.newBuilder()
        
        // Clear existing headers and add modified ones
        modifiedNetworkRequest.headers.forEach { (key, value) ->
            builder.removeHeader(key)
            builder.addHeader(key, value)
        }
        
        // Update body if modified
        modifiedNetworkRequest.body?.let { bodyContent ->
            val mediaType = modifiedNetworkRequest.contentType?.toMediaType()
            val requestBody = bodyContent.toByteArray().toRequestBody(mediaType)
            builder.method(modifiedNetworkRequest.method.name, requestBody)
        }
        
        return builder.build()
    }
    
    private fun createOkHttpResponseFromNetworkResponse(
        originalResponse: Response,
        networkResponse: NetworkResponse
    ): Response {
        val builder = originalResponse.newBuilder()
            .code(networkResponse.statusCode)
            .message(networkResponse.statusMessage)
        
        // Clear existing headers and add modified ones
        networkResponse.headers.forEach { (key, value) ->
            builder.removeHeader(key)
            builder.addHeader(key, value)
        }
        
        // Update body if specified
        networkResponse.body?.let { newBody ->
            val mediaType = networkResponse.contentType?.toMediaType()
                ?: originalResponse.body?.contentType()
            builder.body(newBody.toResponseBody(mediaType))
        }
        
        return builder.build()
    }
    
    
    class Builder {
        private var collector: JarvisNetworkCollector? = null
        private var applyNetworkRulesUseCase: ApplyNetworkRulesUseCase? = null
        private var maxContentLength: Long = MAX_CONTENT_LENGTH
        private var headersToRedact: Set<String> = DEFAULT_REDACTED_HEADERS
        
        fun collector(collector: JarvisNetworkCollector) = apply {
            this.collector = collector
        }
        
        fun rulesUseCase(useCase: ApplyNetworkRulesUseCase) = apply {
            this.applyNetworkRulesUseCase = useCase
        }
        
        fun maxContentLength(length: Long) = apply {
            this.maxContentLength = length
        }
        
        fun redactHeaders(vararg headers: String) = apply {
            this.headersToRedact = headers.map { it.lowercase() }.toSet()
        }
        
        fun build(): JarvisNetworkInterceptor {
            val networkCollector = collector ?: throw IllegalStateException("Collector must be set")
            val rulesUseCase = applyNetworkRulesUseCase ?: throw IllegalStateException("ApplyNetworkRulesUseCase must be set")
            return JarvisNetworkInterceptor(networkCollector, rulesUseCase)
        }
    }
}