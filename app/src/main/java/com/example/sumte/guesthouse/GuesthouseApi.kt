package com.example.sumte.guesthouse

import com.example.sumte.BaseResponse
import com.example.sumte.search.ApiPageResponse
import com.example.sumte.search.GuesthouseItemResponse
import com.example.sumte.search.GuesthouseSearchRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface GuesthouseApi {

    @GET("guesthouse/home")
    suspend fun getGuesthousesHome(
        @Query("keyword") keyword: String?  = null,
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

    @POST("/guesthouse/search")
    suspend fun searchGuesthouses(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Body body: GuesthouseSearchRequest
    ): ApiPageResponse<GuesthouseItemResponse>
}


