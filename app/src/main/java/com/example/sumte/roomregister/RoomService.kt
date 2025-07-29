package com.example.sumte.roomregister

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface RoomService {

    @POST("guesthouses/{id}/rooms")
    fun registerRoom(
        @Path("id") guesthouseId: Int,
        @Body room: RoomRegisterRequest
    ): Call<Void>
}