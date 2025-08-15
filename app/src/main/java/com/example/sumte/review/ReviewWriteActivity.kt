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
import androidx.activity.result.PickVisualMediaRequest
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
import com.example.sumte.ApiClient.reviewService
import com.example.sumte.PhotoOptionsBottomSheet
import com.example.sumte.R
import com.example.sumte.SharedPreferencesManager
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
import java.util.UUID

class ReviewWriteActivity : AppCompatActivity() {
    lateinit var binding: ActivityReviewWriteBinding
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    // Photo Picker를 위한 새로운 런처 추가
    private lateinit var pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>
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
                if (granted) {
                } else {
                    Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }

        // 리뷰 수정 모드/작성 모드에 따라 ReviewPhotoAdapter를 다르게 초기화
        if (isEditMode) {
            reviewPhotoAdapter = ReviewPhotoAdapter(photoList) { position ->
                photoList.removeAt(position)
                reviewPhotoAdapter.notifyItemRemoved(position)
                Log.d("ReviewWriteActivity", "Photo removed at position $position. New photoList size: ${photoList.size}")
                checkIfChanged()
            }
        } else {
            reviewPhotoAdapter = ReviewPhotoAdapter(photoList) { position ->
                photoList.removeAt(position)
                reviewPhotoAdapter.notifyItemRemoved(position)
                togglePhotoSection()
                if (photoList.isEmpty()) {
                    binding.reviewPicShowLl.visibility = View.GONE
                }
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

        // Photo Picker 런처
        pickMediaLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
                uri?.let { addPhoto(it) }
            }

        // 갤러리 런처
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { addPhoto(it) }
            }

        // 바텀시트- 사진 추가 버튼 클릭
        binding.reviewPicAddLl.setOnClickListener {
            val bottomSheet = PhotoOptionsBottomSheet()
            bottomSheet.setOnOptionSelectedListener(object :
                PhotoOptionsBottomSheet.OnOptionSelectedListener {
                override fun onTakePhotoSelected() {
                    requestPermissionsIfNeeded { launchCamera() }
                }

                override fun onSelectFromAlbumSelected() {
                    launchGallery()
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
                if(isEditMode) checkIfChanged() else updateApplyButton()
            }
        }

        // 후기 등록하기 버튼 (작성 및 수정 모드 공통)
        binding.reviewApplyTv.setOnClickListener {
            sendReviewToServer(
                onSuccess = {
                    ReviewSubmittedDialog {
                        finish()
                    }.show(supportFragmentManager, "review_done")
                },
                onFailure = { errorMessage ->
                    Toast.makeText(this@ReviewWriteActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
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

        // 리뷰 수정 모드 초기화
        if (isEditMode) {
            Log.d("ReviewWriteActivity", "Entering edit mode.")
            selectedRating = intent.getIntExtra("score", 0)
            val content = intent.getStringExtra("contents").orEmpty()
            val imageUrls = intent.getSerializableExtra("imageUrls") as? List<String> ?: emptyList()

            Log.d("ReviewWriteActivity", "Received imageUrls from Intent: $imageUrls")
            Log.d("ReviewWriteActivity", "ImageUrls is empty: ${imageUrls.isEmpty()}")

            if (photoList.isEmpty() && imageUrls.isNotEmpty()) {
                photoList.addAll(imageUrls.map { Uri.parse(it) })
            }
            Log.d("ReviewWriteActivity", "PhotoList size after adding images: ${photoList.size}")

            originalContent = intent.getStringExtra("contents").orEmpty()
            originalRating = intent.getIntExtra("score", 0)
            originalImageUrls = imageUrls

            updateStars(listOf(
                binding.starEmpty1Iv, binding.starEmpty2Iv,
                binding.starEmpty3Iv, binding.starEmpty4Iv, binding.starEmpty5Iv
            ), selectedRating)

            binding.reviewContentEt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    checkIfChanged()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            binding.reviewContentEt.setText(originalContent)
            binding.reviewApplyTv.text = "완료"

            if (imageUrls.isNotEmpty()) {
                binding.reviewPicShowLl.visibility = View.VISIBLE
                Log.d("ReviewWriteActivity", "reviewPicShowLl visibility set to VISIBLE.")
            } else {
                Log.d("ReviewWriteActivity", "ImageUrls is empty, setting reviewPicShowLl visibility to GONE.")
                binding.reviewPicShowLl.visibility = View.GONE
            }

            binding.reviewWriteTitleTv.text = "후기 수정하기"
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
        //카메라 권한
        permissions.add(android.Manifest.permission.CAMERA)

        // 안드로이드 버전에 따라 갤러리 접근 권한 분기
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        // } else {
        // permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)

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
        checkIfChanged()
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

    // Photo Picker 실행 함수
    private fun launchGallery() {
        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // private fun launchGallery() {
    // galleryLauncher.launch("image/*")
    // }

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

    private fun sendReviewToServer(onSuccess: () -> Unit, onFailure: (String) -> Unit) = lifecycleScope.launch {
        val isEditMode = intent.getBooleanExtra("isEditMode", false)
        val reviewId = intent.getLongExtra("reviewId", -1)
        val roomId = intent.getLongExtra("roomId", -1)
        val contents = binding.reviewContentEt.text.toString().trim()
        val score = selectedRating
        val imageUrls = intent.getSerializableExtra("imageUrls") as? List<String> ?: emptyList()

        if (roomId == -1L || contents.isEmpty() || score == 0) {
            onFailure("내용과 별점을 모두 입력하세요")
            return@launch
        }

        try {
            val currentImageUrls = photoList.map { it.toString() }
            val isImagesChanged = currentImageUrls.sorted() != originalImageUrls.sorted()

            Log.d("ReviewDebug", "Is images changed?: $isImagesChanged")
            Log.d("ReviewDebug", "Original images list: $originalImageUrls")
            Log.d("ReviewDebug", "Current images list: $currentImageUrls")
            Log.d("ReviewDebug", "reviewId?: $reviewId")

            if (isEditMode && isImagesChanged) {
                Log.d("ReviewDebug", "Images were changed, starting upload process.")
                val uploadedImageUris: List<ImageUri> = withContext(Dispatchers.IO) {
                    val uris = mutableListOf<ImageUri>()
                    for (uri in photoList) {
                        if (uri.toString().startsWith("http")) {
                            uris.add(ImageUri(uri.toString()))
                            continue
                        }

                        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                        val fileName = "REVIEW_${UUID.randomUUID()}.jpg"

                        val presignedUrlResponse = ApiClient.reviewService
                            .getPresignedUrls(listOf(fileName), "REVIEW", reviewId)
                        if (!presignedUrlResponse.isSuccessful) {
                            Log.e("ReviewDebug", "Presigned URL 발급 실패: ${presignedUrlResponse.code()}")
                            throw Exception("Presigned URL 발급 실패")
                        }
                        val presignedUrl = presignedUrlResponse.body()
                            ?.firstOrNull()?.presignedUrl
                            ?: throw Exception("Presigned URL이 비어있습니다.")

                        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw Exception("이미지 읽기 실패")

                        val uploadResponse = OkHttpClient().newCall(
                            Request.Builder()
                                .url(presignedUrl)
                                .put(bytes.toRequestBody(mimeType.toMediaType()))
                                .build()
                        ).execute()

                        if (!uploadResponse.isSuccessful) {
                            Log.e("ReviewDebug", "S3 업로드 실패: ${uploadResponse.code}")
                            throw IOException("S3 업로드 실패")
                        }
                        uris.add(ImageUri(presignedUrl.substringBefore("?")))
                    }
                    uris
                }

                Log.d("ReviewDebug", "Calling putReviewImages with Body: $uploadedImageUris")
                val imageReplaceResponse = ApiClient.reviewService.putReviewImages(
                    "REVIEW",
                    reviewId,
                    uploadedImageUris
                )
                if (imageReplaceResponse.isSuccessful) {
                    Log.d("ReviewDebug", "Image replacement successful.")
                } else {
                    Log.e("ReviewDebug", "Image replacement failed: ${imageReplaceResponse.code()}")
                }
            } else {
                Log.d("ReviewDebug", "Images were not changed or not in edit mode. Skipping image API call.")
            }

            val isContentChanged = contents != originalContent
            val isRatingChanged = selectedRating != originalRating

            if (isEditMode && (isContentChanged || isRatingChanged)) {
                Log.d("ReviewDebug", "Content or rating changed, calling patchReview.")
                val reviewRequest = ReviewRequest2(
                    roomId = roomId,
                    contents = contents,
                    score = score
                )
                val patchResponse = ApiClient.reviewService.patchReview(reviewId, reviewRequest)
                if (patchResponse.isSuccessful) {
                    Log.d("ReviewDebug", "patchReview successful.")
                } else {
                    Log.e("ReviewDebug", "patchReview failed: ${patchResponse.code()}")
                }
            }

            if (!isEditMode) {
                Log.d("ReviewDebug", "Not in edit mode, new review will be submitted.")
            }

            onSuccess()

        } catch (e: Exception) {
            Log.e("ReviewAPI", "오류: ${e.localizedMessage}", e)
            onFailure("오류: ${e.localizedMessage}")
        }
    }
}