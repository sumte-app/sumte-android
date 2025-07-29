package com.example.sumte.ImageUpload

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sumte.databinding.ActivityImageUploadBinding
import kotlinx.coroutines.launch
import java.io.File


class ImageUploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageUploadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uploadBtn.setOnClickListener {
            val fileName = binding.fileNameEt.text.toString().trim()
            val contentType = binding.contentTypeEt.text.toString().trim()

            if (fileName.isEmpty() || contentType.isEmpty()) {
                binding.resultUrlTv.text = "파일 이름과 Content-Type을 입력해주세요"
                return@setOnClickListener
            }

            val imageFile = copyAssetToCache(fileName)
            if (imageFile == null) {
                binding.resultUrlTv.text = "assets 폴더에서 파일을 찾을 수 없습니다: $fileName"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val url = S3Uploader.uploadImageToS3(this@ImageUploadActivity, imageFile)

                if (url != null) {
                    binding.resultUrlTv.text = url
                    Log.d("ImageUpload", "Uploaded image URL: $url")
                } else {
                    binding.resultUrlTv.text = "업로드 실패"
                    Log.e("ImageUpload", "S3 업로드 실패")
                }
            }
        }
    }

    private fun copyAssetToCache(fileName: String): File? {
        return try {
            val inputStream = assets.open(fileName)
            val outFile = File(cacheDir, fileName)
            inputStream.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}