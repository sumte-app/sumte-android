package com.example.sumte

import com.example.sumte.review.MyReview

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
