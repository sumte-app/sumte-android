package com.example.sumte

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class FavoriteResponse(
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val content: List<FavoriteContent>,
    val number: Int,
    val sort: Sort,
    val numberOfElements: Int,
    val pageable: Pageable,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)

// content 리스트의 각 아이템
data class FavoriteContent(
    val guesthouseId: Long,
    val guesthouseName: String,
    val imageUrl: String?,
    val location: String?,
    val price: String?,
    val checkInTime: String?,
    val rating: String?,
    val reviewCount: String?
    // 찜한 게스트하우스의 다른 정보 (이미지, 가격, 위치 등) 추가
    // 서버 응답에 따라 필드 추가: 예를 들어, "imageUrl", "location", "price" 등
    // 만약 서버에서 이 정보들을 함께 주지 않으면, 별도로 게스트하우스 정보를 조회
    // guesthouseId와 guesthouseName만 포함한다고 가정
)

interface FavoriteService {
    // 찜 추가
    @POST("/api/favorites/{guesthouseId}")
    suspend fun addFavorite(@Path("guesthouseId") guesthouseId: Long): Response<Unit>

    // 찜 삭제
    @DELETE("/api/favorites/{guesthouseId}")
    suspend fun deleteFavorite(@Path("guesthouseId") guesthouseId: Long): Response<Unit>

    // 찜 목록 조회
    @GET("/api/favorites")
    suspend fun getMyFavorites(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createdAt,DESC"
    ): Response<FavoriteResponse>

    @GET("/api/favorites/status/{guesthouseId}")
    suspend fun getFavoriteStatus(@Path("guesthouseId") guesthouseId: Long): Response<Boolean>
}