package com.example.sumte.review

data class ReviewResponse(
    val content: List<Review>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int, // 현재 페이지
    val size: Int,   // 페이지당 아이템 수
    val empty: Boolean
)
