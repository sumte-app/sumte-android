package com.example.sumte.review

import com.example.sumte.MyReviewPage
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
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

    @POST("reviews")
    suspend fun postReview(
        @Body body: ReviewRequest
    ): Response<Unit>

    @GET("reviews/myReviews")
    suspend fun getMyReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createdAt,DESC"
    ): Response<MyReviewPage>

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") reviewId: Long): Response<Unit>

    @GET("/api/reviews")
    suspend fun getAllReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ReviewResponse>

    @PATCH("reviews/{id}")
    suspend fun patchReview(
        @Path("reviewId") reviewId: Long,
        @Body body: ReviewRequest
    ): Response<Unit>
}
