package com.example.sumte.ImageUpload

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface S3Service {
    @GET("s3/presigned-url")
    suspend fun getPresignedUrl(
        @Query("fileName") fileName: String,
        @Query("contentType") contentType: String
    ): Response<ResponseBody>
}