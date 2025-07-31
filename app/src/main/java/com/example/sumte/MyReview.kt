package com.example.sumte

data class MyReview(
    val id: Long,
    val imageUrl: String?,
    val contents: String,
    val score: Int,
    val authorNickname: String,
    val createdAt: String
)
