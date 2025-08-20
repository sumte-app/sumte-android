package com.example.sumte

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
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

    //예약상세
    @GET("reservations/{id}")
    suspend fun getReservationDetail(
        @Path("id") reservationId: Int
    ): Response<ReservationDetailResponse>

    //예약취소
    @PATCH("reservations/{id}")
    suspend fun cancelReservation(
        @Path("id") reservationId: Int
    ): Response<ReservationResponse<Nothing?>>


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
//여기가 키미의 reservationId와 충돌
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
    val roomId: Long,
    val reservedAt: String, //추가
    val reservationId: Int // 추가
)

// 상세 조회 Response
data class ReservationDetailResponse(
    val success: Boolean,
    val code: String?,
    val message: String?,
    val data: ReservationDetailData?
)

// 상세 예약 정보
//여기도이 id이름맞추기..?
data class ReservationDetailData(
    val reservationId: Int,
    val guestHouseName: String,
    val roomName: String,
    val imageUrl: String?,
    val adultCount: Int,
    val childCount: Int,
    val startDate: String,
    val endDate: String,
    val status: String,
    val nightCount: Int,
    val totalPrice: Int,
    val reservedAt: String
)





