package com.example.sumte

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://sumteapi.duckdns.org/swagger-ui/index.html#/%EB%A6%AC%EB%B7%B0/createReview"   // ★ 서버 주소로 교체

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val reviewService: ReviewService by lazy {
        retrofit.create(ReviewService::class.java)
    }
}