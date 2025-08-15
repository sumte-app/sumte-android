package com.example.sumte.payment

import com.google.gson.annotations.SerializedName


enum class PaymentMethod { CARD, KAKAOPAY }

data class PaymentRequest(
    val reservationId: Int,
    val amount: Int,
    val paymentMethod: PaymentMethod
)

data class PaymentApproveRequest(
    val reservationId: Int,
    @SerializedName("pg_token")
    val pg_token: String,
    val appScheme: String?
)



