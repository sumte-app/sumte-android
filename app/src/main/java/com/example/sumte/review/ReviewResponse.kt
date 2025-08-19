package com.example.sumte.review

data class ReviewItem(
    val id: Int,
    val imageUrls: List<String>?,
    val contents: String?,
    val score: Int,
    val authorNickname: String,
    val createdAt: String,
    val roomName: String,
    val guesthouseName: String
)

// ReviewPageResponse.kt
data class ReviewPageResponse(
    val content: List<ReviewItem>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int, // 현재 페이지 번호
    val size: Int,   // 페이지 크기
    val empty: Boolean,
    val last: Boolean
)
