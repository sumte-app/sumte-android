package com.example.sumte.review

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
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

    var originalContent = ""
    var originalRating = 0
    private lateinit var originalImageUrls: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val isEditMode = intent.getBooleanExtra("isEditMode", false)

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

        // 리뷰 수정 모드
        if (isEditMode) {
            Log.d("ReviewWriteActivity", "Entering edit mode.")
            selectedRating = intent.getIntExtra("score", 0)
            val content = intent.getStringExtra("contents").orEmpty()
            val imageUrls = intent.getSerializableExtra("imageUrls") as? List<String> ?: emptyList()

            // 디버깅 로그: Intent에서 받아온 이미지 URL 리스트 확인
            Log.d("ReviewWriteActivity", "Received imageUrls from Intent: $imageUrls")
            Log.d("ReviewWriteActivity", "ImageUrls is empty: ${imageUrls.isEmpty()}")

            // photoList에 기존 이미지 URL을 Uri로 변환하여 추가
            if (photoList.isEmpty() && imageUrls.isNotEmpty()) {
                photoList.addAll(imageUrls.map { Uri.parse(it) })
            }
            Log.d("ReviewWriteActivity", "PhotoList size after adding images: ${photoList.size}")

            // 기존 리뷰 내용 및 점수 저장
            originalContent = intent.getStringExtra("contents").orEmpty()
            originalRating = intent.getIntExtra("score", 0)
            originalImageUrls = imageUrls

            // 별점 UI 업데이트
            updateStars(listOf(
                binding.starEmpty1Iv, binding.starEmpty2Iv,
                binding.starEmpty3Iv, binding.starEmpty4Iv, binding.starEmpty5Iv
            ), selectedRating)

            // 내용 변경 시 버튼 상태 갱신
            binding.reviewContentEt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    checkIfChanged()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // EditText에 기존 내용 설정
            binding.reviewContentEt.setText(originalContent)
            binding.reviewApplyTv.text = "완료"

            // 이미지 리스트가 비어있지 않으면 RecyclerView에 이미지를 띄워준다.
            if (imageUrls.isNotEmpty()) {
                binding.reviewPicShowLl.visibility = View.VISIBLE
                Log.d("ReviewWriteActivity", "reviewPicShowLl visibility set to VISIBLE.")

                // 어댑터 초기화 (삭제 리스너 포함)
                reviewPhotoAdapter = ReviewPhotoAdapter(photoList) { position ->
                    photoList.removeAt(position)
                    reviewPhotoAdapter.notifyItemRemoved(position)
                    Log.d("ReviewWriteActivity", "Photo removed at position $position. New photoList size: ${photoList.size}")
                    // 이미지 삭제 후 버튼 상태 갱신
                    checkIfChanged()
                }

                // RecyclerView에 어댑터와 LayoutManager 설정
                binding.reviewPicShowRv.apply {
                    layoutManager = LinearLayoutManager(this@ReviewWriteActivity, LinearLayoutManager.HORIZONTAL, false)
                    adapter = reviewPhotoAdapter
                }
                Log.d("ReviewWriteActivity", "Adapter and LayoutManager for reviewPicShowRv are set.")
            } else {
                Log.d("ReviewWriteActivity", "ImageUrls is empty, setting reviewPicShowLl visibility to GONE.")
                binding.reviewPicShowLl.visibility = View.GONE
            }

            // 상단 텍스트 업데이트
            binding.reviewWriteTitleTv.text = "후기 수정하기"

            // 리뷰 수정후 등록
            binding.reviewApplyTv.setOnClickListener {
                val reviewId = intent.getLongExtra("reviewId", -1)
                val updatedContent = binding.reviewContentEt.text.toString()

                val request = ReviewRequest(
                    roomId = intent.getLongExtra("roomId", -1),
                    contents = updatedContent,
                    score = selectedRating,
                    imageUrls = photoList.map { it.toString() }
                )

                lifecycleScope.launch {
                    try {
                        // IO 스레드에서 API 호출
                        val response = withContext(Dispatchers.IO) {
                            ApiClient.reviewService.patchReview(reviewId, request)
                        }

                        // UI 스레드에서 결과 처리
                        if (response.isSuccessful) {
                            Toast.makeText(this@ReviewWriteActivity, "리뷰가 수정되었습니다", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ReviewWriteActivity, "수정 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ReviewWriteActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 현재 액티비티를 종료하여 이전 화면(ReviewManage)으로 돌아감
        binding.reviewWriteCancelIv.setOnClickListener {
            finish()
        }

    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "edit_review_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
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

    private fun checkIfChanged() {
        val currentContent = binding.reviewContentEt.text.toString()
        val isContentChanged = currentContent != originalContent
        val isRatingChanged = selectedRating != originalRating
        val currentImageUrls = photoList.map { it.toString() }
        val isImagesChanged = currentImageUrls.sorted() != originalImageUrls.sorted()

        if (isContentChanged || isRatingChanged || isImagesChanged) {
            binding.reviewApplyDefaultTv.visibility = View.GONE
            binding.reviewApplyTv.visibility = View.VISIBLE
        } else {
            binding.reviewApplyDefaultTv.visibility = View.VISIBLE
            binding.reviewApplyTv.visibility = View.GONE
        }
    }

    private fun sendReviewToServer() = lifecycleScope.launch {
        val isEditMode = intent.getBooleanExtra("isEditMode", false)
        val reviewId = intent.getLongExtra("reviewId", -1)
        val roomId   = intent.getLongExtra("roomId", -1)
        val contents = binding.reviewContentEt.text.toString().trim()
        val score    = selectedRating
        val imageUrls = intent.getSerializableExtra("imageUrls") as? List<String> ?: emptyList()

        if (roomId == -1L || contents.isEmpty() || score == 0) {
            Toast.makeText(this@ReviewWriteActivity,
                "내용과 별점을 모두 입력하세요", Toast.LENGTH_SHORT).show()
            return@launch
        }

        try {
            val imageUrl: String? = withContext(Dispatchers.IO) {
                if (photoList.isEmpty()) return@withContext null
                if (photoList.first().toString().startsWith("http")) return@withContext photoList.first().toString()

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
            val body = ReviewRequest(roomId = roomId, imageUrls = imageUrls, contents = contents, score = score)

            val resp = if (isEditMode && reviewId != -1L) {
                ApiClient.reviewService.patchReview(reviewId, body)
            } else {
                ApiClient.reviewService.postReview(body)
            }

            if (resp.isSuccessful) {
                Toast.makeText(this@ReviewWriteActivity, "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@ReviewWriteActivity, "등록 실패: ${resp.code()}", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this@ReviewWriteActivity, "오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}