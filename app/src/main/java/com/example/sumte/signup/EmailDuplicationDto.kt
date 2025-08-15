package com.example.sumte.signup

data class EmailDuplicationDto(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: String?
)