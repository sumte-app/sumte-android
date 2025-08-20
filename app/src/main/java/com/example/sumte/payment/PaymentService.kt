package com.example.sumte.payment


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PaymentService {
    @POST("/payments/request")
    suspend fun requestPayment(
        @Body body: PaymentRequest
    ): Response<PaymentResponse>

    @PATCH("/payments/{id}/approve")
    suspend fun approvePayment(
        @Path("id") paymentId: Int,
        @Query("pg_token") pgToken: String
    ): Response<PaymentApproveResponse<PaymentApproveData>>

    @PATCH("/payments/{id}/approve/manual")
    suspend fun approvePaymentManual(
        @Path("id") paymentId: Int
    ): Response<PaymentApproveManualResponse>
}