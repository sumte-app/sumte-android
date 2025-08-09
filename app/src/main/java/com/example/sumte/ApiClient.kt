package com.example.sumte

import android.content.Context
import android.content.SharedPreferences
import com.example.sumte.ImageUpload.S3Service
import com.example.sumte.login.AuthService
import com.example.sumte.review.ReviewService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
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

        val requestBuilder = originalRequest.newBuilder()

        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}

object ApiClient {
    private const val BASE_URL = "https://sumteapi.duckdns.org/"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
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