package com.example.sumte

data class MyReviewPage(
    val totalPages: Int,
    val content: List<MyReview>
)

data class MyReview(
    val id: Long,
    val imageUrl: String?,
    val contents: String,
    val score: Int,
    val authorNickname: String,
    val createdAt: String
)
