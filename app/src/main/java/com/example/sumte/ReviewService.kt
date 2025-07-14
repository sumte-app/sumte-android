package com.example.sumte

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class ReviewRequest(
    val roomId: Long,
    val imageUrl: String?,
    val contents: String,
    val score: Int
)

interface ReviewService {
    @GET("/s3/presigned-url")
    suspend fun getPresignedUrl(
        @Query("fileName")   fileName: String,
        @Query("contentType") contentType: String
    ): Response<String>           // presigned URL 문자열 그대로 반환한다고 가정

    // 후기 등록
    @POST("/api/reviews")
    suspend fun postReview(
        @Body body: ReviewRequest
    ): Response<Unit>        // 응답 JSON이 있으면 dto로 교체

    // 내가 쓴 후기 목록 불러오기
    @GET("/api/reviews/myreviews")
    suspend fun getMyReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createdAt,DESC"
    ): Response<MyReviewPage>

    @DELETE("/api/reviews/{id}")
    suspend fun deleteReview(@Path("id") reviewId: Long): Response<Unit>
}
