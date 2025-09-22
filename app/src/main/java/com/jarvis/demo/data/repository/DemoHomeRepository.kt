package com.jarvis.demo.data.repository

import android.util.Log
import com.jarvis.demo.data.api.FakeStoreApiService
import com.jarvis.demo.data.api.RestfulApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoHomeRepository @Inject constructor(
    private val fakeStoreApiService: FakeStoreApiService,
    private val restfulApiService: RestfulApiService
) {

    private var lastApiCallTime = 0L
    private var cachedApiResults: Triple<Response<*>?, Response<*>?, Response<*>?>? = null

    suspend fun refreshData(): Triple<Response<*>?, Response<*>?, Response<*>?> = coroutineScope {
        try {
            // Only make API calls every 10 seconds to reduce load
            val currentTime = System.currentTimeMillis()
            val timeSinceLastCall = currentTime - lastApiCallTime

            if (timeSinceLastCall < 10000) { // 10 seconds throttle
                return@coroutineScope cachedApiResults ?: Triple(null, null, null)
            }

            lastApiCallTime = currentTime

            val productsCall = async {
                withTimeoutOrNull(3000) { // 3s timeout
                    fakeStoreApiService.getAllProducts()
                }
            }
            val categoriesCall = async {
                withTimeoutOrNull(3000) {
                    fakeStoreApiService.getAllCategories()
                }
            }
            val objectsCall = async {
                withTimeoutOrNull(3000) {
                    restfulApiService.getAllObjects()
                }
            }

            // Execute API calls with timeout protection and log results
            val productsResponse = productsCall.await()
            val categoriesResponse = categoriesCall.await()
            val objectsResponse = objectsCall.await()

            Log.d("DemoHomeRepository", "Products response: ${productsResponse?.code() ?: "timeout"}")
            Log.d("DemoHomeRepository", "Categories response: ${categoriesResponse?.code() ?: "timeout"}")
            Log.d("DemoHomeRepository", "Objects response: ${objectsResponse?.code() ?: "timeout"}")

            val result = Triple(productsResponse, categoriesResponse, objectsResponse)
            cachedApiResults = result // Cache the results
            result
        } catch (e: Exception) {
            Log.w("DemoHomeRepository", "Some API calls failed", e)
            cachedApiResults ?: Triple(null, null, null) // Return cached or null values
        }
    }
}