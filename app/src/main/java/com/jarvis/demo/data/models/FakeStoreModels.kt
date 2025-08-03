package com.jarvis.demo.data.models

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Rating
)

data class Rating(
    val rate: Double,
    val count: Int
)

data class CreateProductRequest(
    val title: String,
    val price: Double,
    val description: String,
    val image: String,
    val category: String
)

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val password: String,
    val name: Name,
    val address: Address,
    val phone: String
)

data class Name(
    val firstname: String,
    val lastname: String
)

data class Address(
    val city: String,
    val street: String,
    val number: Int,
    val zipcode: String,
    val geolocation: Geolocation
)

data class Geolocation(
    val lat: String,
    val long: String
)

data class Cart(
    val id: Int,
    val userId: Int,
    val date: String,
    val products: List<CartProduct>
)

data class CartProduct(
    val productId: Int,
    val quantity: Int
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String
)