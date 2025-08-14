package com.example.sumte.payment


enum class PaymentMethod { CARD, KAKAOPAY }

data class PaymentRequest(
    val reservationId: Int,
    val amount: Int,
    val paymentMethod: PaymentMethod
)

data class PaymentApproveRequest(
    val reservationId: Int,
    val pg_token: String
)



