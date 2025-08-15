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
    ): Response<MyReservationResponse>
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


// 내 예약 응답
data class MyReservationResponse(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: MyReservationData?
)


data class MyReservationData(
    val content: List<MyReservationItem>
)

data class MyReservationItem(
    val id: Int,
    val guestHouseName: String,
    val roomName: String,
    val imageUrl: String?,
    val startDate: String,
    val endDate: String,
    val adultCount: Int,
    val childCount: Int,
    val nightCount: Int,
    val status: String,
    val canWriteReview: Boolean,
    val reviewWritten: Boolean,
    val roomId: Long
)





