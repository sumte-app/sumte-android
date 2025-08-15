package com.example.sumte.payment

import android.content.Context
import android.util.Log
import com.example.sumte.RetrofitClient
import retrofit2.Response

class PaymentRepository(context: Context) {

    private val appContext = context.applicationContext

    private fun jwt(): String? =
        appContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
            .getString("access_token", null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private fun authedServiceOrNull(): PaymentService? =
        jwt()?.let { token -> RetrofitClient.createPaymentService(token) }

    // 결제 준비
    suspend fun requestKakaoPay(reservationId: Int, amount: Int): PaymentData {
        val service = authedServiceOrNull() ?: error("로그인이 필요합니다.")
        val req = PaymentRequest(
            reservationId = reservationId,
            amount = amount,
            paymentMethod = PaymentMethod.KAKAOPAY
        )
        val res = service.requestPayment(req)
        if (!res.isSuccessful) throw retrofit2.HttpException(res)
        val body = res.body() ?: error("Empty body")
        if (!body.success) error("${body.code}: ${body.message}")
        return body.data
    }

    // 결제 승인
    suspend fun approvePayment(
        id: Int,
        token: String
    ): Response<PaymentApproveResponse<PaymentApproveData>>? {
        val service = authedServiceOrNull() ?: return null


        val resp = service.approvePayment(id, token)

        if (!resp.isSuccessful) {
            Log.e("PayRepo", "approve HTTP ${resp.code()} err=${resp.errorBody()?.string()}")
        } else {
            Log.d("PayRepo", "approve HTTP ${resp.code()}")
        }
        return resp
    }
}
