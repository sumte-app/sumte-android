package com.example.sumte.review

data class ReviewResponse(
    val content: List<Review>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int,
    val size: Int,
    val empty: Boolean
)
