package com.jarvis.demo.data.api

import com.jarvis.demo.data.models.Cart
import com.jarvis.demo.data.models.CreateProductRequest
import com.jarvis.demo.data.models.LoginRequest
import com.jarvis.demo.data.models.LoginResponse
import com.jarvis.demo.data.models.Product
import com.jarvis.demo.data.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FakeStoreApiService {
    
    @GET("products")
    suspend fun getAllProducts(): Response<List<Product>>
    
    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<Product>
    
    @GET("products")
    suspend fun getProductsWithLimit(@Query("limit") limit: Int): Response<List<Product>>
    
    @GET("products")
    suspend fun getProductsWithSort(@Query("sort") sort: String): Response<List<Product>>
    
    @GET("products/categories")
    suspend fun getAllCategories(): Response<List<String>>
    
    @GET("products/category/{category}")
    suspend fun getProductsInCategory(@Path("category") category: String): Response<List<Product>>
    
    @POST("products")
    suspend fun createProduct(@Body product: CreateProductRequest): Response<Product>
    
    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: CreateProductRequest): Response<Product>
    
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Product>
    
    @GET("carts")
    suspend fun getAllCarts(): Response<List<Cart>>
    
    @GET("carts/{id}")
    suspend fun getCart(@Path("id") id: Int): Response<Cart>
    
    @GET("carts")
    suspend fun getCartsWithLimit(@Query("limit") limit: Int): Response<List<Cart>>
    
    @GET("carts")
    suspend fun getCartsWithSort(@Query("sort") sort: String): Response<List<Cart>>
    
    @GET("carts")
    suspend fun getCartsWithDateRange(
        @Query("startdate") startDate: String,
        @Query("enddate") endDate: String
    ): Response<List<Cart>>
    
    @GET("carts/user/{userId}")
    suspend fun getUserCarts(@Path("userId") userId: Int): Response<List<Cart>>
    
    @POST("carts")
    suspend fun createCart(@Body cart: Cart): Response<Cart>
    
    @PUT("carts/{id}")
    suspend fun updateCart(@Path("id") id: Int, @Body cart: Cart): Response<Cart>
    
    @DELETE("carts/{id}")
    suspend fun deleteCart(@Path("id") id: Int): Response<Cart>
    
    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<User>
    
    @GET("users")
    suspend fun getUsersWithLimit(@Query("limit") limit: Int): Response<List<User>>
    
    @GET("users")
    suspend fun getUsersWithSort(@Query("sort") sort: String): Response<List<User>>
    
    @POST("users")
    suspend fun createUser(@Body user: User): Response<User>
    
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: User): Response<User>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<User>
    
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}