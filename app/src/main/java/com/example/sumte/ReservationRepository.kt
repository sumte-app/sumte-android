package com.example.sumte.reservation

import android.content.Context
import android.util.Log
import com.example.sumte.MyReservationItem
import com.example.sumte.ReservationData
import com.example.sumte.ReservationDetailData
import com.example.sumte.ReservationDetailResponse
import com.example.sumte.ReservationRequest
import com.example.sumte.ReservationResponse
import com.example.sumte.ReservationService
import com.example.sumte.RetrofitClient
import com.example.sumte.RetrofitClient.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ReservationRepository(private val context: Context) {

    private val reservationService = RetrofitClient.createReservationServiceWithoutAuth()


    // 예약 생성
    suspend fun createReservation(request: ReservationRequest): Response<ReservationResponse<ReservationData>>? {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPref.getString("access_token", null)?.trim()
        Log.d("Reservation_JWT", "JWT Token: [$jwtToken]")

        if (jwtToken.isNullOrEmpty()) return null

        val reservationService = RetrofitClient.createReservationService(jwtToken)
        return reservationService.createReservation(request)
    }

    // 내 예약 조회
    suspend fun getMyReservations(): List<MyReservationItem> {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPref.getString("access_token", null)?.trim()
        Log.d("Reservation_JWT", "JWT Token for getMyReservations: [$jwtToken]")

        if (jwtToken.isNullOrEmpty()) {
            Log.e("ReservationRepository", "empty")
            return emptyList()
        }

        val reservationService = RetrofitClient.createReservationService(jwtToken)
        Log.d("Reservation_Service", "{$reservationService}")


        return try {
            val response = reservationService.getMyReservation()
            if (response.isSuccessful) {
                Log.d("ReservationRepository", "예약 조회 성공: ${response.body()}")
                response.body()?.data?.content ?: emptyList()
            } else {
                Log.e("ReservationRepository", "예약 조회 실패: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    //예약 조회
    suspend fun getReservationDetail(reservationId: Int): ReservationDetailData? {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<ReservationDetailResponse> =
                    reservationService.getReservationDetail(reservationId)
                if (response.isSuccessful) {
                    response.body()?.data
                } else {
                    Log.e("ReservationRepository", "예약 상세 조회 실패: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    //예약취소
    suspend fun cancelReservation(reservationId: Int): ReservationResponse<Nothing?>? {
        return try {
            val response = reservationService.cancelReservation(reservationId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
