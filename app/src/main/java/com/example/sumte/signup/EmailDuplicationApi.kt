package com.example.sumte.signup

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface EmailDuplicationApi {
    @GET("/users/signup/duplicate/email")
    suspend fun checkEmailDuplicate(
        @Query("email") email: String
    ): Response<EmailDuplicationDto>
}