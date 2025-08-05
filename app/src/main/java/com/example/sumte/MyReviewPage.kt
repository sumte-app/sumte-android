package com.example.sumte

data class MyReviewPage(
    val totalPages: Int,
    val totalElements: Int,
    val size: Int,
    val content: List<MyReview>,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)
