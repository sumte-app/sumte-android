package com.example.sumte.housedetail

import com.example.sumte.review.ReviewPageResponse
import com.example.sumte.review.ReviewService

class ReviewRepository(private val api: ReviewService) {
    suspend fun fetchGuesthouseReviews(
        guesthouseId: Long,
        page: Int,
        size: Int,
        sort: List<String> = listOf("createdAt,DESC")
    ): Result<ReviewPageResponse> {
        return try {
            val res = api.getGuesthouseReviews(guesthouseId, page, size, sort)
            if (res.isSuccessful) {
                val body = res.body()
                if (body != null) Result.success(body)
                else Result.failure(IllegalStateException("Empty body"))
            } else {
                Result.failure(IllegalStateException("HTTP ${res.code()} ${res.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
