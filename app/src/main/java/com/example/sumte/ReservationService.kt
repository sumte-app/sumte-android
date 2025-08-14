package com.example.sumte

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// 예약 API
interface ReservationService {
    @POST("reservations")
    suspend fun createReservation(
        @Body request: ReservationRequest
    ): Response<ReservationResponse<ReservationData>>

    // 내 예약 목록 조회
    @GET("reservations/my")
    suspend fun getMyReservation(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ReservationResponse<MyReservationResponse>>
}

// 예약생성 요청
data class ReservationRequest(
    val roomId: Int,
    val adultCount: Int,
    val childCount: Int,
    val startDate: String,
    val endDate: String
)

// 예약요청 응답
data class ReservationResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String? = null,
    val data: T?
)
data class ReservationData(
    val reservationId: Int
)


// 내 예약 목록 응답 아이템
data class MyReservationResponse(
    val reservationId: Int,
    val roomName: String,
    val startDate: String,
    val endDate: String,
    val adultCount: Int,
    val childCount: Int
)




