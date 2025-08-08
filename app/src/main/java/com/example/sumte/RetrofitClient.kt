package com.example.sumte

import com.example.sumte.guesthouse.GuesthouseApi
import com.example.sumte.roomregister.RoomRegisterService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue
import com.example.sumte.housedetail.RoomService

object RetrofitClient {
    private const val BASE_URL = "https://sumteapi.duckdns.org/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // 조회 API
    val roomService: RoomService by lazy { instance.create(RoomService::class.java)}

    // 등록 API
    val roomRegisterService: RoomRegisterService by lazy { instance.create(RoomRegisterService::class.java)}

    val api: GuesthouseApi by lazy {
        instance.create(GuesthouseApi::class.java)
    }
}