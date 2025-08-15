package com.example.sumte

import com.example.sumte.signup.EmailSendReq
import com.example.sumte.signup.EmailSendRes
import com.example.sumte.signup.EmailVerifyReq
import com.example.sumte.signup.EmailVerifyRes
import com.example.sumte.signup.SignUpRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/users/signup")
    fun signUp(@Body request: SignUpRequest): Call<Void>

    // ApiService.kt
    @GET("users/signup/duplicate/nickname")
    fun checkNickname(@Query("nickname") nickname: String): Call<NicknameResponse>

    @POST("/email/send")
    suspend fun sendEmail(@Body req: EmailSendReq): EmailSendRes

    @POST("/email/verify")
    suspend fun verifyEmail(@Body req: EmailVerifyReq): EmailVerifyRes


}