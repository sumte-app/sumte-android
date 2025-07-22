package com.example.sumte

data class NicknameResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: String?
)