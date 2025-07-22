package com.example.sumte


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/users/signup")
    fun signUp(@Body request: SignUpRequest): Call<Void>

    // ApiService.kt
    @GET("/users/signup/duplicate/nickname")
    fun checkNickname(@Query("nickname") nickname: String): Call<NicknameResponse>
}