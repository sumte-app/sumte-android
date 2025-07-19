package com.example.a8thumcproject.model

// model/NicknameResponse.kt
data class NicknameResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: String?
)

