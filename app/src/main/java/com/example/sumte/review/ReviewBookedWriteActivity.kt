package com.example.sumte.review

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
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
import java.io.File
import java.io.IOException
import java.util.UUID

class ReviewBookedWriteActivity : AppCompatActivity() {

    lateinit var binding: ActivityReviewWriteBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    private val photoList = mutableListOf<Uri>()
    private lateinit var reviewPhotoAdapter: ReviewPhotoAdapter
    private var cameraImageUri: Uri? = null

    // Intent로 받아올 ID
    private var reviewId: Long = -1
    private var roomId: Long = -1

    // 내용을 추가하지 않고 창을 닫는 경우을 고려한 변수
    private var isContentModified = false
    private var reservationId: Int = -1

    // 변경 감지를 위한 원본 데이터 (항상 빈 값으로 시작)
    private var originalContent = ""
    private var originalRating = 0
    private var originalImageUrls = emptyList<String>()

    private var selectedRating = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent에서 ID 값 받기
        reviewId = intent.getLongExtra("BookedReviewId", -1L)
        roomId = intent.getLongExtra("BookedRoomId", -1L)
        reservationId = intent.getIntExtra("BookedReservationId", -1)
        // ID 값이 없으면 액티비티 종료
        if (reviewId == -1L || roomId == -1L) {
            Toast.makeText(this, "리뷰 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // UI 초기 설정 (항상 '작성' 모드)
        binding.reviewWriteTitleTv.text = "후기 작성하기"
        binding.reviewApplyTv.text = "완료"
        // 초기에는 버튼 비활성화
        binding.reviewApplyDefaultTv.visibility = View.VISIBLE
        binding.reviewApplyTv.visibility = View.GONE

        initLaunchers()
        initAdapter()
        initListeners()
    }

    private fun initLaunchers() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.all { it.value }) {
                    launchCamera() // 권한 허용 시 카메라 즉시 실행
                } else {
                    Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && cameraImageUri != null) {
                    addPhoto(cameraImageUri!!)
                }
            }

        pickMediaLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
                uri?.let { addPhoto(it) }
            }
    }

    private fun initAdapter() {
        reviewPhotoAdapter = ReviewPhotoAdapter(photoList) { position ->
            photoList.removeAt(position)
            reviewPhotoAdapter.notifyItemRemoved(position)
            checkIfChanged()
            togglePhotoSection()
        }
        binding.reviewPicShowRv.apply {
            adapter = reviewPhotoAdapter
            layoutManager = LinearLayoutManager(this@ReviewBookedWriteActivity, RecyclerView.HORIZONTAL, false)
        }
    }

    private fun initListeners() {
        // 사진 추가 버튼
        binding.reviewPicAddLl.setOnClickListener {
            val bottomSheet = PhotoOptionsBottomSheet()
            bottomSheet.setOnOptionSelectedListener(object : PhotoOptionsBottomSheet.OnOptionSelectedListener {
                override fun onTakePhotoSelected() {
                    requestCameraPermission()
                }
                override fun onSelectFromAlbumSelected() {
                    launchGallery()
                }
            })
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        // 별점 평가
        val starViews = listOf(
            binding.starEmpty1Iv, binding.starEmpty2Iv, binding.starEmpty3Iv,
            binding.starEmpty4Iv, binding.starEmpty5Iv
        )
        starViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = index + 1
                updateStars(starViews, selectedRating)
                checkIfChanged() // 별점 변경 시에도 체크
            }
        }

        // 텍스트 변경 감지
        binding.reviewContentEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { checkIfChanged() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 완료 버튼
        binding.reviewApplyTv.setOnClickListener {
            sendReviewToServer(
                onSuccess = {
//                    ReviewSubmittedDialog { finish() }.show(supportFragmentManager, "review_done")
                    showSuccessDialogAndSetResult()
                },
                onFailure = { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 취소 버튼
        binding.reviewWriteCancelIv.setOnClickListener {
            if (!isContentModified) {
                deletePlaceholderReview()
            }
            finish()
        }
    }

    private fun sendReviewToServer(onSuccess: () -> Unit, onFailure: (String) -> Unit) = lifecycleScope.launch {
        val contents = binding.reviewContentEt.text.toString().trim()
        val score = selectedRating

        if (contents.isEmpty() || score == 0) {
            onFailure("내용과 별점을 모두 입력하세요")
            return@launch
        }

        try {
            // 이 액티비티는 항상 '수정' 로직만 수행
            val currentImageUrls = photoList.map { it.toString() }
            val isImagesChanged = currentImageUrls.sorted() != originalImageUrls.sorted()

            // 이미지가 변경되었으면 업로드
            if (isImagesChanged) {
                Log.d("ReviewDebug", "Images were changed, starting upload process.")
                val uploadedImageUris: List<ImageUri> = withContext(Dispatchers.IO) {
                    val uris = mutableListOf<ImageUri>()
                    for (uri in photoList) {
                        // BookedWrite에서는 http로 시작하는 기존 이미지가 없으므로 체크 불필요
                        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                        val fileName = "REVIEW_${UUID.randomUUID()}.jpg"

                        val presignedUrlResponse = ApiClient.reviewService.getPresignedUrls(listOf(fileName), "REVIEW", reviewId)
                        if (!presignedUrlResponse.isSuccessful) throw Exception("Presigned URL 발급 실패")

                        val presignedUrl = presignedUrlResponse.body()?.firstOrNull()?.presignedUrl ?: throw Exception("Presigned URL이 비어있습니다.")

                        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw Exception("이미지 읽기 실패")

                        val uploadResponse = OkHttpClient().newCall(Request.Builder().url(presignedUrl).put(bytes.toRequestBody(mimeType.toMediaType())).build()).execute()
                        if (!uploadResponse.isSuccessful) throw IOException("S3 업로드 실패: ${uploadResponse.code}")

                        uris.add(ImageUri(presignedUrl.substringBefore("?")))
                    }
                    uris
                }

                // 업로드된 이미지 목록을 서버에 알려줌
                val imageReplaceResponse = ApiClient.reviewService.putReviewImages("REVIEW", reviewId, uploadedImageUris)
                if (!imageReplaceResponse.isSuccessful) Log.e("ReviewDebug", "Image replacement failed: ${imageReplaceResponse.code()}")
            }

            // 텍스트나 별점이 변경되었으면 업데이트
            val isContentChanged = contents != originalContent
            val isRatingChanged = score != originalRating
            if (isContentChanged || isRatingChanged) {
                val reviewRequest = ReviewRequest2(roomId = roomId, contents = contents, score = score)
                val patchResponse = ApiClient.reviewService.patchReview(reviewId, reviewRequest)
                if (!patchResponse.isSuccessful) Log.e("ReviewDebug", "patchReview failed: ${patchResponse.code()}")
            }

//            setResult(Activity.RESULT_OK)

            onSuccess()

        } catch (e: Exception) {
            Log.e("ReviewAPI", "오류: ${e.localizedMessage}", e)
            onFailure("오류가 발생했습니다: ${e.localizedMessage}")
        }
    }

    // 이하 헬퍼 함수들

    private fun checkIfChanged() {
        val currentContent = binding.reviewContentEt.text.toString()
        val isContentChanged = currentContent != originalContent
        val isRatingChanged = selectedRating != originalRating
        val currentImageUrls = photoList.map { it.toString() }
        val isImagesChanged = currentImageUrls.sorted() != originalImageUrls.sorted()
        val hasChanged = isContentChanged || isRatingChanged || isImagesChanged

        isContentModified = hasChanged

        if (hasChanged) {
            binding.reviewApplyDefaultTv.visibility = View.GONE
            binding.reviewApplyTv.visibility = View.VISIBLE
        } else {
            binding.reviewApplyDefaultTv.visibility = View.VISIBLE
            binding.reviewApplyTv.visibility = View.GONE
        }
    }

    private fun addPhoto(uri: Uri) {
        if (photoList.size >= 5) {
            Toast.makeText(this, "사진은 최대 5장까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        photoList.add(uri)
        reviewPhotoAdapter.notifyItemInserted(photoList.lastIndex)
        togglePhotoSection()
        checkIfChanged()
    }

    private fun togglePhotoSection() {
        binding.reviewPicShowLl.visibility = if (photoList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun requestCameraPermission() {
        val permission = android.Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            permissionLauncher.launch(arrayOf(permission))
        }
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile("camera_photo_", ".jpg", cacheDir)
        cameraImageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        cameraLauncher.launch(cameraImageUri)
    }

    private fun launchGallery() {
        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { idx, iv ->
            iv.setImageResource(
                if (idx < rating) R.drawable.star_fill
                else R.drawable.star_empty
            )
        }
    }

    // 성공 다이얼로그를 보여주고, 이전 화면으로 결과를 반환하는 함수.
    private fun showSuccessDialogAndSetResult() {
        ReviewSubmittedDialog {
            val resultIntent = Intent().apply {
                putExtra("completedReservationId", reservationId)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }.show(supportFragmentManager, "review_done")
    }

    private fun deletePlaceholderReview() {
        if (reviewId != -1L) {
            Log.d("ReviewBookedWriteActivity", "내용이 수정되지 않았으므로 임시 리뷰(id: $reviewId)를 삭제합니다.")
            lifecycleScope.launch {
                try {
                    ApiClient.reviewService.deleteReview(reviewId)
                } catch (e: Exception) {
                    Log.e("ReviewBookedWriteActivity", "임시 리뷰 삭제 실패: ${e.message}")
                }
            }
        }
    }
}