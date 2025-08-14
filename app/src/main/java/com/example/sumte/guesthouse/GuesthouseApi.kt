package com.example.sumte.guesthouse

import com.example.sumte.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


// GuesthouseApi.kt
interface GuesthouseApi {

    @GET("guesthouse/home")
    suspend fun getGuesthousesHome(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<BaseResponse<PageDto<GuesthouseHomeItemDto>>>


    @GET("guesthouse/{id}")
    suspend fun getGuesthouse(@Path("id") id: Long): Response<BaseResponse<GuesthouseDto>>

    @GET("/images")
    suspend fun getImages(
        @Query("ownerType") ownerType: String,
        @Query("ownerId") ownerId: Long
    ): Response<List<ImageDto>>
}


