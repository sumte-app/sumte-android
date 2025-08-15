package com.example.sumte.housedetail

data class UnavailableDatesResponse(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: List<String>?
)
