package com.example.sumte

data class BaseResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: Any?
)