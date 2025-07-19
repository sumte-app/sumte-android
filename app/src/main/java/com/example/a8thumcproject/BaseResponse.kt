package com.example.a8thumcproject.model

data class BaseResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: Any?
)