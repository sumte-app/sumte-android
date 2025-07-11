package com.example.sumte

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ActivityReviewWriteBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class ReviewWriteActivity:AppCompatActivity() {
    lateinit var binding: ActivityReviewWriteBinding
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val photoList = mutableListOf<Uri>()
    private lateinit var reviewPhotoAdapter: ReviewPhotoAdapter

    private var cameraImageUri: Uri? = null
    private var selectedRating = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityReviewWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.all { it.value }
            if (!granted) {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 리싸이클러 및 어댑터 초기화
        reviewPhotoAdapter = ReviewPhotoAdapter(photoList) { position ->
            photoList.removeAt(position)
            reviewPhotoAdapter.notifyItemRemoved(position)

            if (photoList.isEmpty()) {
                binding.reviewPicShowLl.visibility = View.GONE
            }
        }

        binding.reviewPicShowRv.apply {
            adapter = reviewPhotoAdapter
            layoutManager = LinearLayoutManager(this@ReviewWriteActivity, RecyclerView.HORIZONTAL, false)
        }

        // 카메라 런처
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraImageUri != null) {
                photoList.add(cameraImageUri!!)
                reviewPhotoAdapter.notifyItemInserted(photoList.lastIndex)
                binding.reviewPicShowLl.visibility = View.VISIBLE
            }
        }

        // 갤러리 런처
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                photoList.add(it)
                reviewPhotoAdapter.notifyItemInserted(photoList.lastIndex)
                binding.reviewPicShowLl.visibility = View.VISIBLE
            }
        }

        // 바텀시트- 사진 추가 버튼 클릭시
        binding.reviewPicAddLl.setOnClickListener {
            val bottomSheet = PhotoOptionsBottomSheet()
            bottomSheet.setOnOptionSelectedListener(object:PhotoOptionsBottomSheet.OnOptionSelectedListener{
                override fun onTakePhotoSelected() {
                    requestPermissionsIfNeeded {
                        launchCamera()
                    }
                }

                override fun onSelectFromAlbumSelected() {
                    requestPermissionsIfNeeded {
                        launchGallery()
                    }
                }
            })
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
//        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//            if (success && cameraImageUri != null) {
//                photoList.add(cameraImageUri!!)
//                reviewPhotoAdapter.notifyItemInserted(photoList.lastIndex)
//                binding.reviewPicShowLl.visibility = View.VISIBLE
//            }
//        }
//
//        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            uri?.let {
//                photoList.add(it)
//                reviewPhotoAdapter.notifyItemInserted(photoList.lastIndex)
//                binding.reviewPicShowLl.visibility = View.VISIBLE
//            }
//        }

        // 별점 평가
        val starViews = listOf(
            binding.starEmpty1Iv,
            binding.starEmpty2Iv,
            binding.starEmpty3Iv,
            binding.starEmpty4Iv,
            binding.starEmpty5Iv
        )

        // ⭐ 2. 각 별에 클릭 리스너 세팅
        starViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = index + 1            // 1~5점
                updateStars(starViews, selectedRating)
                updateApplyButton()
            }
        }

        // 후기 등록하기 버튼
        binding.reviewApplyTv.setOnClickListener {
            //유효성 검사 필요
            ReviewSubmittedDialog {
                finish()
            }.show(supportFragmentManager, "review_done")
        }
        // 리뷰 작성 영역 선택시 클릭 리스너
        binding.reviewContentLl.setOnClickListener {
            binding.reviewContentLl.setBackgroundResource(R.drawable.round_style_review_selected)
        }
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

}