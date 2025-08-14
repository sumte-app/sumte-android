package com.example.sumte.payment

import com.google.gson.annotations.SerializedName

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

data class PaymentApproveResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T
)

data class PaymentApproveData(
    val aid: String,
    val tid: String,
    val cid: String,
    @SerializedName("partner_order_id") val partnerOrderId: String,
    @SerializedName("partner_user_id") val partnerUserId: String,
    @SerializedName("payment_method_type") val paymentMethodType: String,
    val amount: PaymentAmount
)

data class PaymentAmount(
    val total: Int,
    @SerializedName("tax_free") val taxFree: Int,
    val vat: Int
)
