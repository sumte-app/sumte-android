package com.example.sumte

import android.util.Log
import com.example.sumte.guesthouse.GuesthouseApi
import com.example.sumte.roomregister.RoomRegisterService
import com.example.sumte.housedetail.RoomService
import com.example.sumte.payment.PaymentRepository
import com.example.sumte.payment.PaymentService
import com.example.sumte.signup.EmailDuplicationApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://sumteapi.duckdns.org/"

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
    val roomService: RoomService by lazy { instance.create(RoomService::class.java) }

    // 등록 API
    val roomRegisterService: RoomRegisterService by lazy { instance.create(RoomRegisterService::class.java) }

    val api: GuesthouseApi by lazy { instance.create(GuesthouseApi::class.java) }

    // 결제 API
//    val paymentService: PaymentService by lazy { instance.create(PaymentService::class.java) }
//    val paymentRepository: PaymentRepository by lazy { PaymentRepository(paymentService) }

    // 예약 API 헤더 설정
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


    fun createPaymentService(token: String): PaymentService {
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(req)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentService::class.java)
    }

    val emailDuplicationApi: EmailDuplicationApi =
        instance.create(EmailDuplicationApi::class.java)

}
