package com.jarvis.features.inspector.internal.domain.entity

import org.junit.Test
import org.junit.Assert.*

class NetworkTransactionTest {

    private fun createSampleRequest() = NetworkRequest(
        url = "https://api.example.com/users",
        method = HttpMethod.GET,
        headers = mapOf("Content-Type" to "application/json")
    )

    private fun createSampleResponse() = NetworkResponse(
        statusCode = 200,
        statusMessage = "OK",
        headers = mapOf("Content-Type" to "application/json"),
        body = """{"users": []}""",
        bodySize = 15L
    )

    @Test
    fun `default transaction has pending status`() {
        val request = createSampleRequest()
        val transaction = NetworkTransaction(request = request)

        assertEquals(TransactionStatus.PENDING, transaction.status)
        assertTrue(transaction.isPending)
        assertFalse(transaction.isCompleted)
        assertFalse(transaction.isFailed)
        assertNull(transaction.response)
        assertNull(transaction.endTime)
        assertNull(transaction.error)
    }

    @Test
    fun `transaction generates unique ids`() {
        val request = createSampleRequest()
        val transaction1 = NetworkTransaction(request = request)
        val transaction2 = NetworkTransaction(request = request)

        assertNotEquals(transaction1.id, transaction2.id)
    }

    @Test
    fun `duration returns null when not completed`() {
        val request = createSampleRequest()
        val transaction = NetworkTransaction(request = request)

        assertNull(transaction.duration)
    }

    @Test
    fun `duration calculates correctly when completed`() {
        val request = createSampleRequest()
        val startTime = 1000L
        val endTime = 2500L
        val transaction = NetworkTransaction(
            request = request,
            startTime = startTime,
            endTime = endTime
        )

        assertEquals(1500L, transaction.duration)
    }

    @Test
    fun `withResponse updates transaction correctly`() {
        val request = createSampleRequest()
        val response = createSampleResponse()
        val original = NetworkTransaction(request = request)

        val updated = original.withResponse(response)

        assertEquals(TransactionStatus.COMPLETED, updated.status)
        assertEquals(response, updated.response)
        assertNotNull(updated.endTime)
        assertTrue(updated.isCompleted)
        assertFalse(updated.isPending)
        assertFalse(updated.isFailed)
    }

    @Test
    fun `withError updates transaction correctly`() {
        val request = createSampleRequest()
        val errorMessage = "Network timeout"
        val original = NetworkTransaction(request = request)

        val updated = original.withError(errorMessage)

        assertEquals(TransactionStatus.FAILED, updated.status)
        assertEquals(errorMessage, updated.error)
        assertNotNull(updated.endTime)
        assertTrue(updated.isFailed)
        assertFalse(updated.isPending)
        assertFalse(updated.isCompleted)
    }

    @Test
    fun `status flags work correctly for all states`() {
        val request = createSampleRequest()

        val pendingTransaction = NetworkTransaction(
            request = request,
            status = TransactionStatus.PENDING
        )
        assertTrue(pendingTransaction.isPending)
        assertFalse(pendingTransaction.isCompleted)
        assertFalse(pendingTransaction.isFailed)

        val completedTransaction = NetworkTransaction(
            request = request,
            status = TransactionStatus.COMPLETED
        )
        assertFalse(completedTransaction.isPending)
        assertTrue(completedTransaction.isCompleted)
        assertFalse(completedTransaction.isFailed)

        val failedTransaction = NetworkTransaction(
            request = request,
            status = TransactionStatus.FAILED
        )
        assertFalse(failedTransaction.isPending)
        assertFalse(failedTransaction.isCompleted)
        assertTrue(failedTransaction.isFailed)
    }

    @Test
    fun `data class equality works correctly`() {
        val request = createSampleRequest()
        val transaction1 = NetworkTransaction(
            id = "test-id",
            request = request,
            status = TransactionStatus.PENDING
        )
        val transaction2 = NetworkTransaction(
            id = "test-id",
            request = request,
            status = TransactionStatus.PENDING
        )
        val transaction3 = NetworkTransaction(
            id = "different-id",
            request = request,
            status = TransactionStatus.PENDING
        )

        assertEquals(transaction1, transaction2)
        assertNotEquals(transaction1, transaction3)
        assertEquals(transaction1.hashCode(), transaction2.hashCode())
    }
}

class NetworkRequestTest {

    @Test
    fun `hasBody returns true when body is not null or blank`() {
        val requestWithBody = NetworkRequest(
            url = "https://api.example.com",
            method = HttpMethod.POST,
            body = """{"name": "John"}"""
        )
        val requestWithEmptyBody = NetworkRequest(
            url = "https://api.example.com",
            method = HttpMethod.POST,
            body = ""
        )
        val requestWithoutBody = NetworkRequest(
            url = "https://api.example.com",
            method = HttpMethod.GET
        )

        assertTrue(requestWithBody.hasBody)
        assertFalse(requestWithEmptyBody.hasBody)
        assertFalse(requestWithoutBody.hasBody)
    }

    @Test
    fun `isGraphQL detects GraphQL requests correctly`() {
        val graphQLQuery = NetworkRequest(
            url = "https://api.example.com/graphql",
            method = HttpMethod.POST,
            contentType = "application/json",
            body = """{"query": "{ users { id name } }"}"""
        )
        val graphQLMutation = NetworkRequest(
            url = "https://api.example.com/graphql",
            method = HttpMethod.POST,
            contentType = "application/json",
            body = """{"mutation": "createUser(name: \"John\")"}"""
        )
        val regularRequest = NetworkRequest(
            url = "https://api.example.com/users",
            method = HttpMethod.GET,
            contentType = "application/json"
        )

        assertTrue(graphQLQuery.isGraphQL)
        assertTrue(graphQLMutation.isGraphQL)
        assertFalse(regularRequest.isGraphQL)
    }

    @Test
    fun `protocol extraction works correctly`() {
        val httpsRequest = NetworkRequest(
            url = "https://api.example.com/users",
            method = HttpMethod.GET
        )
        val httpRequest = NetworkRequest(
            url = "http://api.example.com/users",
            method = HttpMethod.GET
        )

        assertEquals("HTTPS", httpsRequest.protocol)
        assertEquals("HTTP", httpRequest.protocol)
    }

    @Test
    fun `host extraction works correctly`() {
        val request = NetworkRequest(
            url = "https://api.example.com/users/123",
            method = HttpMethod.GET
        )

        assertEquals("api.example.com", request.host)
    }

    @Test
    fun `host extraction handles malformed urls`() {
        val malformedRequest = NetworkRequest(
            url = "not-a-valid-url",
            method = HttpMethod.GET
        )

        assertEquals("not-a-valid-url", malformedRequest.host)
    }

    @Test
    fun `path extraction works correctly`() {
        val requestWithPath = NetworkRequest(
            url = "https://api.example.com/users/123",
            method = HttpMethod.GET
        )
        val requestRootPath = NetworkRequest(
            url = "https://api.example.com",
            method = HttpMethod.GET
        )

        assertEquals("/users/123", requestWithPath.path)
        assertEquals("/", requestRootPath.path)
    }

    @Test
    fun `path extraction handles malformed urls`() {
        val malformedRequest = NetworkRequest(
            url = "not-a-valid-url",
            method = HttpMethod.GET
        )

        assertEquals("/", malformedRequest.path)
    }
}

class HttpMethodTest {

    @Test
    fun `fromString returns correct HttpMethod for valid strings`() {
        assertEquals(HttpMethod.GET, HttpMethod.fromString("GET"))
        assertEquals(HttpMethod.POST, HttpMethod.fromString("POST"))
        assertEquals(HttpMethod.PUT, HttpMethod.fromString("PUT"))
        assertEquals(HttpMethod.DELETE, HttpMethod.fromString("DELETE"))
        assertEquals(HttpMethod.PATCH, HttpMethod.fromString("PATCH"))
        assertEquals(HttpMethod.HEAD, HttpMethod.fromString("HEAD"))
        assertEquals(HttpMethod.OPTIONS, HttpMethod.fromString("OPTIONS"))
        assertEquals(HttpMethod.TRACE, HttpMethod.fromString("TRACE"))
        assertEquals(HttpMethod.CONNECT, HttpMethod.fromString("CONNECT"))
    }

    @Test
    fun `fromString handles lowercase input`() {
        assertEquals(HttpMethod.GET, HttpMethod.fromString("get"))
        assertEquals(HttpMethod.POST, HttpMethod.fromString("post"))
        assertEquals(HttpMethod.PUT, HttpMethod.fromString("put"))
    }

    @Test
    fun `fromString returns GET for invalid method`() {
        assertEquals(HttpMethod.GET, HttpMethod.fromString("INVALID"))
        assertEquals(HttpMethod.GET, HttpMethod.fromString(""))
        assertEquals(HttpMethod.GET, HttpMethod.fromString("CUSTOM"))
    }
}