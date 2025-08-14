package com.example.sumte.reservation

import android.content.Context
import android.util.Log
import com.example.sumte.ReservationData
import com.example.sumte.ReservationRequest
import com.example.sumte.ReservationResponse
import com.example.sumte.RetrofitClient
import retrofit2.Response

class ReservationRepository(private val context: Context) {

    suspend fun createReservation(request: ReservationRequest): Response<ReservationResponse<ReservationData>>? {
        // SharedPreferences에서 JWT 가져오기
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPref.getString("access_token", null)?.trim()
        Log.d("Reservation_JWT", "JWT Token: [$jwtToken]")

        if (jwtToken.isNullOrEmpty()) {
            return null // 로그인 안된 상태
        }

        val reservationService = RetrofitClient.createReservationService(jwtToken)
        return reservationService.createReservation(request)
    }
}
