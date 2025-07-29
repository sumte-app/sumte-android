package com.example.sumte.ImageUpload

import android.content.Context
import android.util.Log
import com.example.sumte.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object S3Uploader {

    suspend fun uploadImageToS3(context: Context, imageFile: File): String? {
        val fileName = imageFile.name
        val contentType = getMimeType(fileName) ?: "image/jpeg"

        // 1. Presigned URL 요청
        val response = ApiClient.s3Service.getPresignedUrl(fileName, contentType)
        if (!response.isSuccessful) {
            Log.e("S3Uploader", "Presigned URL 요청 실패: ${response.code()}")
            return null
        }

        val presignedUrl = response.body()?.string() ?: return null

        // 2. withContext로 네트워크 요청을 IO 스레드에서 처리
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = imageFile.asRequestBody(contentType.toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(presignedUrl)
                    .put(requestBody)
                    .build()

                val client = OkHttpClient()
                val uploadResponse = client.newCall(request).execute()

                if (uploadResponse.isSuccessful) {
                    Log.d("S3Uploader", "S3 업로드 성공")
                    presignedUrl.substringBefore("?")
                } else {
                    Log.e("S3Uploader", "S3 업로드 실패: ${uploadResponse.code}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getMimeType(fileName: String): String? {
        return when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".png", true) -> "image/png"
            else -> null
        }
    }
}