package com.example.sumte.guesthouse

import com.example.sumte.MyReviewPage
import com.example.sumte.network.model.ClosedDatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GuestHouseService {
    @GET("/guesthouse/home")
    suspend fun getGuestHouse(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createdAt,DESC"
    ): Response<MyReviewPage>

    interface GuesthouseService {
        @GET("guesthouse/{guesthouseId}/fully-booked-dates")
        suspend fun getClosedDates(@Path("guesthouseId") guesthouseId: Int): ClosedDatesResponse
    }




}