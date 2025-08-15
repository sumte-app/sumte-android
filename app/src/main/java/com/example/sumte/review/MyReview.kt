package com.example.sumte.review

data class MyReview(
    val id: Long,
    val imageUrls: List<String>?,
    val contents: String,
    val score: Int,
    val authorNickname: String,
    val createdAt: String,
    val roomId: Long
)
