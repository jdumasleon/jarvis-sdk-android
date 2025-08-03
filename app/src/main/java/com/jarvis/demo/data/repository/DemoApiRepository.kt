package com.jarvis.demo.data.repository

import com.jarvis.demo.data.api.FakeStoreApiService
import com.jarvis.demo.data.api.RestfulApiService
import com.jarvis.demo.data.models.CreateDeviceRequest
import com.jarvis.demo.data.models.CreateProductRequest
import com.jarvis.demo.data.models.DeviceData
import com.jarvis.demo.data.models.LoginRequest
import com.jarvis.demo.data.models.UpdateDeviceRequest
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DemoApiRepository @Inject constructor(
    private val fakeStoreApi: FakeStoreApiService,
    private val restfulApi: RestfulApiService
) {
    
    private val fakeStoreMethods = listOf(
        "getAllProducts",
        "getProduct",
        "getProductsWithLimit", 
        "getAllCategories",
        "createProduct",
        "getAllCarts",
        "getAllUsers",
        "login"
    )
    
    private val restfulApiMethods = listOf(
        "getAllObjects",
        "getObject",
        "createObject",
        "updateObject",
        "patchObject",
        "deleteObject"
    )
    
    private val sampleProductNames = listOf(
        "Smartphone", "Laptop", "Headphones", "Watch", "Camera",
        "Tablet", "Speaker", "Monitor", "Keyboard", "Mouse"
    )
    
    private val sampleDeviceNames = listOf(
        "iPhone 15", "MacBook Pro", "iPad Air", "Apple Watch",
        "Samsung Galaxy", "Dell XPS", "HP Spectre", "Lenovo ThinkPad"
    )
    
    suspend fun performRandomApiCall(): ApiCallResult {
        val isRestfulApi = Random.nextBoolean()
        val startTime = System.currentTimeMillis()
        
        return try {
            if (isRestfulApi) {
                performRandomRestfulApiCall(startTime)
            } else {
                performRandomFakeStoreApiCall(startTime)
            }
        } catch (e: Exception) {
            ApiCallResult(
                url = if (isRestfulApi) "https://api.restful-api.dev" else "https://fakestoreapi.com",
                host = if (isRestfulApi) "api.restful-api.dev" else "fakestoreapi.com",
                method = "GET",
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                isSuccess = false,
                statusCode = 0,
                error = e.message
            )
        }
    }
    
    private suspend fun performRandomFakeStoreApiCall(startTime: Long): ApiCallResult {
        // Add artificial delay to simulate network
        delay(Random.nextLong(200, 1500))
        
        val method = fakeStoreMethods.random()
        return when (method) {
            "getAllProducts" -> {
                val response = fakeStoreApi.getAllProducts()
                createApiCallResult("https://fakestoreapi.com/products", "GET", startTime, response)
            }
            "getProduct" -> {
                val productId = Random.nextInt(1, 21)
                val response = fakeStoreApi.getProduct(productId)
                createApiCallResult("https://fakestoreapi.com/products/$productId", "GET", startTime, response)
            }
            "getProductsWithLimit" -> {
                val limit = Random.nextInt(1, 11)
                val response = fakeStoreApi.getProductsWithLimit(limit)
                createApiCallResult("https://fakestoreapi.com/products?limit=$limit", "GET", startTime, response)
            }
            "getAllCategories" -> {
                val response = fakeStoreApi.getAllCategories()
                createApiCallResult("https://fakestoreapi.com/products/categories", "GET", startTime, response)
            }
            "createProduct" -> {
                val product = CreateProductRequest(
                    title = sampleProductNames.random(),
                    price = Random.nextDouble(10.0, 1000.0),
                    description = "Test product",
                    image = "https://i.pravatar.cc/150",
                    category = "electronics"
                )
                val response = fakeStoreApi.createProduct(product)
                createApiCallResult("https://fakestoreapi.com/products", "POST", startTime, response)
            }
            "getAllCarts" -> {
                val response = fakeStoreApi.getAllCarts()
                createApiCallResult("https://fakestoreapi.com/carts", "GET", startTime, response)
            }
            "getAllUsers" -> {
                val response = fakeStoreApi.getAllUsers()
                createApiCallResult("https://fakestoreapi.com/users", "GET", startTime, response)
            }
            "login" -> {
                val loginRequest = LoginRequest("mor_2314", "83r5^_")
                val response = fakeStoreApi.login(loginRequest)
                createApiCallResult("https://fakestoreapi.com/auth/login", "POST", startTime, response)
            }
            else -> throw IllegalArgumentException("Unknown method: $method")
        }
    }
    
    private suspend fun performRandomRestfulApiCall(startTime: Long): ApiCallResult {
        // Add artificial delay to simulate network
        delay(Random.nextLong(200, 1500))
        
        val method = restfulApiMethods.random()
        return when (method) {
            "getAllObjects" -> {
                val response = restfulApi.getAllObjects()
                createApiCallResult("https://api.restful-api.dev/objects", "GET", startTime, response)
            }
            "getObject" -> {
                val objectId = Random.nextInt(1, 11).toString()
                val response = restfulApi.getObject(objectId)
                createApiCallResult("https://api.restful-api.dev/objects/$objectId", "GET", startTime, response)
            }
            "createObject" -> {
                val device = CreateDeviceRequest(
                    name = sampleDeviceNames.random(),
                    data = DeviceData(
                        year = Random.nextInt(2020, 2025),
                        price = Random.nextDouble(100.0, 2000.0),
                        cpuModel = "Intel i7",
                        hardDiskSize = "${Random.nextInt(256, 2048)} GB",
                        color = listOf("Silver", "Black", "White", "Gold").random()
                    )
                )
                val response = restfulApi.createObject(device)
                createApiCallResult("https://api.restful-api.dev/objects", "POST", startTime, response)
            }
            "updateObject" -> {
                val objectId = Random.nextInt(1, 11).toString()
                val device = UpdateDeviceRequest(
                    name = sampleDeviceNames.random(),
                    data = DeviceData(
                        year = Random.nextInt(2020, 2025),
                        price = Random.nextDouble(100.0, 2000.0),
                        cpuModel = "Intel i7",
                        hardDiskSize = "${Random.nextInt(256, 2048)} GB",
                        color = listOf("Silver", "Black", "White", "Gold").random()
                    )
                )
                val response = restfulApi.updateObject(objectId, device)
                createApiCallResult("https://api.restful-api.dev/objects/$objectId", "PUT", startTime, response)
            }
            "patchObject" -> {
                val objectId = Random.nextInt(1, 11).toString()
                val device = UpdateDeviceRequest(
                    name = sampleDeviceNames.random(),
                    data = null
                )
                val response = restfulApi.patchObject(objectId, device)
                createApiCallResult("https://api.restful-api.dev/objects/$objectId", "PATCH", startTime, response)
            }
            "deleteObject" -> {
                val objectId = Random.nextInt(1, 11).toString()
                val response = restfulApi.deleteObject(objectId)
                createApiCallResult("https://api.restful-api.dev/objects/$objectId", "DELETE", startTime, response)
            }
            else -> throw IllegalArgumentException("Unknown method: $method")
        }
    }
    
    private fun createApiCallResult(url: String, method: String, startTime: Long, response: Response<*>): ApiCallResult {
        val endTime = System.currentTimeMillis()
        return ApiCallResult(
            url = url,
            host = java.net.URL(url).host,
            method = method,
            startTime = startTime,
            endTime = endTime,
            isSuccess = response.isSuccessful,
            statusCode = response.code(),
            error = if (!response.isSuccessful) "HTTP ${response.code()}" else null
        )
    }
}

data class ApiCallResult(
    val url: String,
    val host: String,
    val method: String,
    val startTime: Long,
    val endTime: Long,
    val isSuccess: Boolean,
    val statusCode: Int,
    val error: String? = null
) {
    val duration: Long get() = endTime - startTime
    val timestamp: String get() = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(startTime)
}