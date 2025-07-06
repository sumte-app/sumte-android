package com.example.sumte

data class LoginResponse(
    val token: String,
    val userId: Long,
    val userName: String
)
