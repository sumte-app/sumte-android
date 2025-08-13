package com.example.sumte

import android.util.Log
import com.example.sumte.guesthouse.GuesthouseApi
import com.example.sumte.roomregister.RoomRegisterService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue
import com.example.sumte.housedetail.RoomService
import com.example.sumte.payment.PaymentRepository
import com.example.sumte.payment.PaymentService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    //원래 private
    private const val BASE_URL = "https://sumteapi.duckdns.org/"


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


    val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val ok = OkHttpClient.Builder().addInterceptor(logging).build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(ok)
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

    // 결제 API
    val paymentService: PaymentService by lazy { instance.create(PaymentService::class.java) }
    val paymentRepository: PaymentRepository by lazy { PaymentRepository(paymentService) }

}
