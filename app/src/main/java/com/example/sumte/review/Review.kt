package com.example.sumte.review

data class Review(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val imageUrls: List<String>? = null,
    val rating: Float = 0f
)
