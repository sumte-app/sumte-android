package com.example.sumte

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SharedPreferencesManager {
    private const val AUTH_TOKEN_KEY = "auth_token"
    // 실제 앱에서는 Context를 받아 SharedPreferences 객체를 초기화.
    var authToken: String? = null // 실제로는 SharedPreferences에서 로드
        get() {
            // 실제 구현: SharedPreferences에서 토큰 로드
            // 예를 들어, val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            // return prefs.getString(AUTH_TOKEN_KEY, null)
            return field // 현재는 임시 변수를 사용
        }
        set(value) {
            field = value
            // 실제 구현: SharedPreferences에 토큰 저장
            // val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).edit()
            // prefs.putString(AUTH_TOKEN_KEY, value)
            // prefs.apply()
        }
}

// 인증 헤더를 추가
class AuthInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = SharedPreferencesManager.authToken

        val requestBuilder = originalRequest.newBuilder()

        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}

object ApiClient {
    private const val BASE_URL = "https://sumteapi.duckdns.org/swagger-ui/index.html#/%EB%A6%AC%EB%B7%B0/createReview"

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