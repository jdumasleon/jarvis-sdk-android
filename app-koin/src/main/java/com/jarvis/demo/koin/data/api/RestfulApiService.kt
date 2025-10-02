package com.jarvis.demo.koin.data.api

import com.jarvis.demo.koin.data.models.CreateDeviceRequest
import com.jarvis.demo.koin.data.models.Device
import com.jarvis.demo.koin.data.models.UpdateDeviceRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RestfulApiService {
    
    @GET("objects")
    suspend fun getAllObjects(): Response<List<Device>>
    
    @GET("objects/{id}")
    suspend fun getObject(@Path("id") id: String): Response<Device>
    
    @POST("objects")
    suspend fun createObject(@Body device: CreateDeviceRequest): Response<Device>
    
    @PUT("objects/{id}")
    suspend fun updateObject(@Path("id") id: String, @Body device: UpdateDeviceRequest): Response<Device>
    
    @PATCH("objects/{id}")
    suspend fun patchObject(@Path("id") id: String, @Body device: UpdateDeviceRequest): Response<Device>
    
    @DELETE("objects/{id}")
    suspend fun deleteObject(@Path("id") id: String): Response<Any>
}