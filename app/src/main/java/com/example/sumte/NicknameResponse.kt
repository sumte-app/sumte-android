package com.example.sumte

data class NicknameResponse(
    val success: Boolean? = null,
    val available: Boolean? = null,
    val duplicated: Boolean? = null,
    val message: String? = null,
    val code: String? = null,
    val data: Any? = null
)

data class ApiError(
    val success: Boolean? = null,
    val code: String? = null,
    val message: String? = null,
    val data: Any? = null
)