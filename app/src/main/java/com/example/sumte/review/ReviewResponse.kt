package com.example.sumte.review

data class ReviewResponse(
//    val content: List<Review>,
//    val totalElements: Int,
//    val totalPages: Int,
//    val number: Int,
//    val size: Int,
//    val empty: Boolean
    val id: Int,
    val imageUrls: List<String>?,
    val contents: String?,
    val score: Int,
    val authorNickname: String,
    val createdAt: String,
    val roomName: String,
    val guesthouseName: String,
    val content: List<ReviewListResponse>
)

data class ReviewListResponse(
    val id: Int,
    val imageUrls: List<String>?,
    val contents: String?,
    val score: Int,
    val authorNickname: String,
    val createdAt: String,
    val roomName: String,
    val guesthouseName: String
)
