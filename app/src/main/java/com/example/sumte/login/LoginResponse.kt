package com.example.sumte.login

data class LoginResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val accessToken: String
)
