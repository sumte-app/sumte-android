package com.example.sumte

import com.google.gson.annotations.SerializedName
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
    @SerializedName(value = "id", alternate = ["guestHouseId"])
    val guesthouseId: Int,
    val guesthouseName: String,
    val imageUrl: String?,
    val location: String?,
    val price: String?,
    val checkInTime: String?,
    val rating: String?,
    val reviewCount: String?
)

interface FavoriteService {
    // 찜 추가
    @POST("/api/favorites/{guesthouseId}")
    suspend fun addFavorite(@Path("guesthouseId") guesthouseId: Int): Response<Unit>

    // 찜 삭제
    @DELETE("/api/favorites/{guesthouseId}")
    suspend fun deleteFavorite(@Path("guesthouseId") guesthouseId: Int): Response<Unit>

    // 찜 목록 조회
    @GET("/api/favorites")
    suspend fun getMyFavorites(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createdAt,DESC"
    ): Response<FavoriteResponse>

    @GET("/api/favorites/status/{guesthouseId}")
    suspend fun getFavoriteStatus(@Path("guesthouseId") guesthouseId: Int): Response<Boolean>
}