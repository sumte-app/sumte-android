package com.example.sumte.review

import com.example.sumte.MyReviewPage
import com.google.gson.annotations.SerializedName
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
    val imageUrls: List<String>?,
    val contents: String,
    val score: Int
)

data class ReviewRequest2(
    val roomId: Long,
    val contents: String,
    val score: Int
)

data class ImageUploadRequest(
    val ownerType: String,
    val ownerId: Long,
    val url: String
)

data class PostImagesResponse(
    val ownerId: Long,
    val imageUrl: String,
    val sortOrder: Int,
    val url: String
)

data class PresignedUrlResponse(
    val originalName: String,
    val imageUrl: String,
    val presignedUrl: String
)

interface ReviewService {
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

    @PATCH("reviews/{reviewId}")
    suspend fun patchReview(
        @Path("reviewId") reviewId: Long,
        @Body body: ReviewRequest2
    ): Response<Unit>

    @GET("s3/presignedUrls")
    suspend fun getPresignedUrls(
        @Query("fileNames") fileNames: List<String>,
        @Query("ownerType") ownerType: String,
        @Query("ownerId") ownerId: Long
    ): Response<List<PresignedUrlResponse>>

    @POST("/images")
    suspend fun postImages(
        @Body request: List<ImageUploadRequest>
    ): Response<List<PostImagesResponse>>
}
