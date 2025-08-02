package com.example.sumte.payment

data class PaymentRequest(
    val reservationId: Int,
    val amount: Int,
    val paymentMethod: String
)

data class PaymentResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: PaymentData
)

data class PaymentData(
    val reservationId: Int,
    val amount: Int,
    val paymentMethod: String
)

