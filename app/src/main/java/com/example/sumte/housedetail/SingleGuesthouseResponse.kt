package com.example.sumte.housedetail


data class SingleGuesthouseResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: GuesthouseDto
)
