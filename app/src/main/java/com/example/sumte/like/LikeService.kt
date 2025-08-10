package com.example.sumte.like

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LikeService {
    // 찜 추가
    @POST("/api/favorites/{guesthouseId}")
    suspend fun addLikes(
        @Path("guesthouseId") guesthouseId: Long
    ): Response<Unit>

    // 찜 취소
    @DELETE("/api/favorites/{guesthouseId}")
    suspend fun removeLikes(
        @Path("guesthouseId") guesthouseId: Long
    ): Response<Unit>

    // 사용자 찜 목록 조회
    @GET("/api/favorites")
    suspend fun getLikes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: List<String>? = null
    ): Response<LikeListResponse>
}