package com.example.sumte.housedetail

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomService {
    // 단일 객실 조회
    @GET("guesthouse/room/{roomId}")
    suspend fun getRoom(@Path("roomId") roomId: Long): RoomResponse<RoomDto>

    // 게하 객실 전체 조회
    @GET("guesthouse/guesthouse/{guesthouseId}/rooms")
    suspend fun getRooms(
        @Path("guesthouseId") guesthouseId: Long,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): RoomResponse<List<RoomDto>>

    @GET("guesthouse/{guesthouseId}")
    suspend fun getGuesthouse(
        @Path("guesthouseId") guesthouseId: Long
    ): SingleGuesthouseResponse
}