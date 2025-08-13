package com.example.sumte

import android.util.Log
import com.example.sumte.guesthouse.GuesthouseApi
import com.example.sumte.roomregister.RoomRegisterService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue
import com.example.sumte.housedetail.RoomService
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient



object RetrofitClient {
    //원래 private
    private const val BASE_URL = "https://sumteapi.duckdns.org/"

    //디버깅용
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun createOkHttpClient(jwtToken: String?): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                jwtToken?.let {
                    builder.addHeader("Authorization", it) // 토큰 그대로 넣기 (Bearer 포함)
                }
                chain.proceed(builder.build())
            }
            .build()
    }


    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().addInterceptor(logging).build())
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

    //예약 api

    fun createReservationService(token: String): ReservationService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofitWithToken = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofitWithToken.create(ReservationService::class.java)
    }

}
