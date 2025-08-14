package com.example.sumte.payment


enum class PaymentMethod { CARD, KAKAOPAY }

data class PaymentRequest(
    val reservationId: Int,
    val amount: Int,
    val paymentMethod: PaymentMethod
)

data class PaymentResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: PaymentData
)

data class PaymentData(
    val paymentId: Int,
    val paymentUrl: String
)

