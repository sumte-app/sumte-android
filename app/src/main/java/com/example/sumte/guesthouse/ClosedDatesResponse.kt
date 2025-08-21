package com.example.sumte.network.model

data class ClosedDatesResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: List<String>
)