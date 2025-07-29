package com.example.sumte.review

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.ApiClient
import com.example.sumte.PhotoOptionsBottomSheet
import com.example.sumte.R
import com.example.sumte.databinding.ActivityReviewWriteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class ReviewWriteActivity:AppCompatActivity() {
    lateinit var binding: ActivityReviewWriteBinding
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val photoList = mutableListOf<Uri>()
    private lateinit var reviewPhotoAdapter: ReviewPhotoAdapter

    private var cameraImageUri: Uri? = null
    private var selectedRating = 0

    private var uploadedImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions.all { it.value }
                if (!granted) {
                    Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }

        // 리싸이클러 및 어댑터 초기화
        reviewPhotoAdapter = ReviewPhotoAdapter(photoList) { position ->
            photoList.removeAt(position)
            reviewPhotoAdapter.notifyItemRemoved(position)
            togglePhotoSection()

            if (photoList.isEmpty()) {
                binding.reviewPicShowLl.visibility = View.GONE
            }
        }

        binding.reviewPicShowRv.apply {
            adapter = reviewPhotoAdapter
            layoutManager =
                LinearLayoutManager(this@ReviewWriteActivity, RecyclerView.HORIZONTAL, false)
        }

        // 카메라 런처
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && cameraImageUri != null) {
                    addPhoto(cameraImageUri!!)
                }
            }

        // 갤러리 런처
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { addPhoto(it) }
            }

        // 바텀시트- 사진 추가 버튼 클릭시
        binding.reviewPicAddLl.setOnClickListener {
            val bottomSheet = PhotoOptionsBottomSheet()
            bottomSheet.setOnOptionSelectedListener(object :
                PhotoOptionsBottomSheet.OnOptionSelectedListener {
                override fun onTakePhotoSelected() {
                    requestPermissionsIfNeeded { launchCamera() }
                }
                override fun onSelectFromAlbumSelected() {
                    requestPermissionsIfNeeded { launchGallery() }
                }
            })
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        // 별점 평가
        val starViews = listOf(
            binding.starEmpty1Iv,
            binding.starEmpty2Iv,
            binding.starEmpty3Iv,
            binding.starEmpty4Iv,
            binding.starEmpty5Iv
        )

        // 각 별에 클릭 리스너
        starViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = if (selectedRating == index + 1) {
                    // 현재 별이 선택된 마지막 별일 경우 → 한 단계 줄이기
                    selectedRating - 1
                } else {
                    // 그 외는 해당 별까지 선택
                    index + 1
                }
                updateStars(starViews, selectedRating)
                updateApplyButton()
            }
        }

        // 후기 등록하기 버튼
        binding.reviewApplyTv.setOnClickListener {
            sendReviewToServer()
            //유효성 검사 필요
            ReviewSubmittedDialog {
                finish()
            }.show(supportFragmentManager, "review_done")
        }

        // 리뷰 작성 영역 선택시 테두리 검정색으로 변경
        binding.reviewContentEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.reviewContentEt.setBackgroundResource(R.drawable.round_style_review_selected)
            } else {
                binding.reviewContentEt.setBackgroundResource(R.drawable.round_style_review_default)
            }
        }
        binding.root.setOnClickListener{
            binding.reviewContentEt.clearFocus()
            hideKeyboard()
        }
    }

    //키보드 숨기기
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun requestPermissionsIfNeeded(onGranted: () -> Unit) {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(android.Manifest.permission.CAMERA)

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            onGranted()
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun addPhoto(uri: Uri) {
        photoList.add(uri)
        reviewPhotoAdapter.notifyItemInserted(photoList.lastIndex)
        togglePhotoSection()
    }

    private fun togglePhotoSection() {
        binding.reviewPicShowLl.visibility =
            if (photoList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile("camera_photo_", ".jpg", cacheDir)
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(cameraImageUri)
    }

    private fun launchGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { idx, iv ->
            iv.setImageResource(
                if (idx < rating) R.drawable.star_fill
                else R.drawable.star_empty
            )
        }
    }
    private fun updateApplyButton() {
        if (selectedRating > 0) {
            binding.reviewApplyDefaultTv.visibility = View.GONE
            binding.reviewApplyTv.visibility = View.VISIBLE
        } else {
            binding.reviewApplyDefaultTv.visibility = View.VISIBLE
            binding.reviewApplyTv.visibility = View.GONE
        }
    }

    // 이미지 -> s3 업로드 후 url 얻기
//    private suspend fun uploadFirstPhotoViaPresignedUrl(): String? = withContext(Dispatchers.IO) {
//        if (photoList.isEmpty()) return@withContext null
//
//        val uri         = photoList.first()
//        val mimeType    = contentResolver.getType(uri) ?: "image/jpeg"
//        val fileName    = "review_${System.currentTimeMillis()}.jpg"
//
//        // presigned URL 요청
//        val preResp = ApiClient.reviewService.getPresignedUrl(fileName, mimeType)
//        if (!preResp.isSuccessful) throw HttpException(preResp)
//        val presignedUrl = preResp.body() ?: throw IllegalStateException("빈 PresignedURL")
//
//        // 파일 바이트 추출
//        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
//            ?: throw IllegalStateException("파일 읽기 실패")
//
//        // PUT 으로 업로드 (OkHttp)
//        val request = Request.Builder()
//            .url(presignedUrl)
//            .put(bytes.toRequestBody(mimeType.toMediaType()))
//            .build()
//
//        val okResp = OkHttpClient().newCall(request).execute()
//        if (!okResp.isSuccessful) throw IOException("S3 업로드 실패: ${okResp.code}")
//
//        presignedUrl.substringBefore("?")
//    }

    private fun sendReviewToServer() = lifecycleScope.launch {
        val roomId   = intent.getLongExtra("roomId", -1)
        val contents = binding.reviewContentEt.text.toString().trim()
        val score    = selectedRating

        if (roomId == -1L || contents.isEmpty() || score == 0) {
            Toast.makeText(this@ReviewWriteActivity,
                "내용과 별점을 모두 입력하세요", Toast.LENGTH_SHORT).show()
            return@launch
        }

        try {
            // 사진 1장을 S3 Presigned URL 로 업로드
            val imageUrl: String? = withContext(Dispatchers.IO) {
                if (photoList.isEmpty()) return@withContext null

                // Presigned URL 발급
                val mimeType = contentResolver.getType(photoList.first()) ?: "image/jpeg"
                val fileName = "review_${System.currentTimeMillis()}.jpg"

                val preResp = ApiClient.reviewService
                    .getPresignedUrl(fileName, mimeType)
                if (!preResp.isSuccessful) throw HttpException(preResp)
                val presignedUrl = preResp.body() ?: throw IllegalStateException("빈 PresignedURL")

                // 파일 바이트 읽기
                val bytes = contentResolver.openInputStream(photoList.first())
                    ?.use { it.readBytes() }
                    ?: throw IllegalStateException("이미지 읽기 실패")

                // PUT 업로드
                val req = Request.Builder()
                    .url(presignedUrl)
                    .put(bytes.toRequestBody(mimeType.toMediaType()))
                    .build()
                OkHttpClient().newCall(req).execute().use { ok ->
                    if (!ok.isSuccessful) throw IOException("업로드 실패: ${ok.code}")
                }

                // public URL 반환 (쿼리스트링 앞부분)
                presignedUrl.substringBefore("?")
            }

            // 리뷰 JSON 전송
            val body = ReviewRequest(roomId = roomId,
                imageUrl = imageUrl,
                contents = contents,
                score = score)

            val resp = ApiClient.reviewService.postReview(body)
            if (resp.isSuccessful) {
                Toast.makeText(this@ReviewWriteActivity,
                    "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@ReviewWriteActivity,
                    "등록 실패: ${resp.code()}",
                    Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this@ReviewWriteActivity,
                "오류: ${e.localizedMessage}",
                Toast.LENGTH_SHORT).show()
        }
    }
}