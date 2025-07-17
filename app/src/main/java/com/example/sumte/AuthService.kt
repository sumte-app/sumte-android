package com.example.sumte

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("users/login")  // baseUrl + "auth/login" => 전체 URL
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}