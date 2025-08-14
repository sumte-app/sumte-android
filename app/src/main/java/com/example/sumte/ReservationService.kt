package com.example.sumte

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// 요청 Body
data class ReservationRequest(
    val roomId: Int,
    val adultCount: Int,
    val childCount: Int,
    val startDate: String,
    val endDate: String
)

// 응답 Body
data class ReservationResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String? = null,
    val data: T?
)

data class ReservationData(
    val reservationId: Int
)

// 예약 API
interface ReservationService {
    @POST("reservations")
    suspend fun createReservation(
        @Body request: ReservationRequest
    ): Response<ReservationResponse<ReservationData>>
}


