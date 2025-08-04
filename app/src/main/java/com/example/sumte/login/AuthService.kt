package com.example.sumte.login

import com.example.sumte.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {

    @POST("users/login")  // baseUrl + "auth/login" => 전체 URL
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("users")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>
}