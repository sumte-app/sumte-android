package com.example.sumte

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.sumte.ImageUpload.S3Service
import com.example.sumte.like.LikeService
import com.example.sumte.login.AuthService
import com.example.sumte.review.ReviewService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SharedPreferencesManager {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    }

    var authToken: String?
        get() = prefs.getString("access_token", null)
        set(value) {
            prefs.edit().putString("access_token", value).apply()
        }
}

// 인증 헤더
class AuthInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = SharedPreferencesManager.authToken
        Log.d("AuthInterceptor", "보낼 토큰 = $token") // 토큰 제대로 전송되는지 확인

        // 디버깅 로그: SharedPreferences에서 가져온 토큰 값 확인
        Log.d("TOKEN_CHECK", "SharedPreferences에서 가져온 토큰: $token")
        if (token == null) {
            Log.e("TOKEN_CHECK", "토큰이 존재하지 않음! 로그인이 필요한 작업일 수 있습니다.")
        }

        val requestBuilder = originalRequest.newBuilder()

        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()

        // 최종 요청 헤더에 토큰이 포함되었는지 확인
        Log.d("TOKEN_CHECK", "최종 요청에 Authorization 헤더 포함 여부: ${request.header("Authorization") != null}")

        return chain.proceed(request)
    }
}

object ApiClient {
    private const val BASE_URL = "https://sumteapi.duckdns.org/"

    // 1. HTTP 로깅 인터셉터 생성
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // BODY 레벨로 설정하여 요청/응답 전문 출력
    }

    private val client: OkHttpClient by lazy {
//        OkHttpClient.Builder()
//            .addInterceptor(AuthInterceptor())
//            .build()
        OkHttpClient.Builder()
            // 2. AuthInterceptor 앞에 로깅 인터셉터를 추가
            // 인터셉터는 추가된 순서대로 실행됩니다.
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val reviewService: ReviewService by lazy {
        retrofit.create(ReviewService::class.java)
    }

    val favoriteService: FavoriteService by lazy {
        retrofit.create(FavoriteService::class.java)
    }

    val s3Service: S3Service by lazy{
        retrofit.create(S3Service::class.java)
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val likeService: LikeService by lazy {
        retrofit.create(LikeService::class.java)
    }
}