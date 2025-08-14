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
        @Path("guesthouseId") guesthouseId: Int
    ): Response<Unit>

    // 찜 취소
    @DELETE("/api/favorites/{guesthouseId}")
    suspend fun removeLikes(
        @Path("guesthouseId") guesthouseId: Int
    ): Response<Unit>

    // 사용자 찜 목록 조회
//    @GET("/api/favorites")
//    suspend fun getLikes(
//        @Query("page") page: Int = 0,
//        @Query("size") size: Int = 10,
//        @Query("sort") sort: List<String>? = null
//    ): Response<LikeListResponse>
    @GET("/api/favorites")
    suspend fun checkFavorites(
        @Query("guesthouseIds") guesthouseIds: List<Int>
    ): Response<List<Int>>

    @GET("/api/favorites")
    suspend fun getLikes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20 // 한 번에 불러올 찜 개수 (필요시 조절)
    ): Response<LikedGuesthouseResponse>
}