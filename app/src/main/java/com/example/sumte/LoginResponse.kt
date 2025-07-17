package com.example.sumte

data class LoginResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val accessToken: String
)
