package com.example.sumte.payment

class PaymentRepository(private val service: PaymentService) {

    suspend fun requestKakaoPay(reservationId: Int, amount: Int): PaymentData {
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
}