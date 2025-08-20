package com.example.sumte.review

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.ApiClient
import com.example.sumte.R
import com.example.sumte.SharedPreferencesManager
import com.example.sumte.databinding.CustomSnackbarBinding
import com.example.sumte.databinding.FragmentReviewManageBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ReviewManage: Fragment() {
    private var _binding: FragmentReviewManageBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy{ ReviewManageAdapter(this) }
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentReviewManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reviewManageArrowIv.setOnClickListener {
            requireActivity().finish()
        }
        binding.reviewManageRv.adapter = adapter
        binding.reviewManageRv.layoutManager = LinearLayoutManager(requireContext())
        loadUserReviews(0)

        binding.reviewManageRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = adapter.itemCount

                if (!isLoading && !isLastPage && lastVisibleItem >= totalItemCount - 3) {
                    currentPage++
                    loadUserReviews(currentPage)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.d("ReviewManage", "onResume() called. Reloading reviews.")
        currentPage = 0
        isLastPage = false
        loadUserReviews(currentPage)
    }

    fun loadUserReviews(page: Int) {
        isLoading = true

        val token = SharedPreferencesManager.authToken
        Log.d("REVIEW_API", "access_token=$token")
        Log.d("REVIEW_API", "요청 page=$page, size=10, sort=createdAt,DESC")

        lifecycleScope.launch {
            try {
                val response = ApiClient.reviewService.getMyReviews(page = page)
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("리뷰 응답", body.toString())
                    if (body != null) {
                        val reviewList = body.content
                        val totalElements = body.totalElements
                        isLastPage = body.last

                        if (reviewList.isNotEmpty()) {
                            if (page == 0) {
                                adapter.setItems(reviewList)
                            } else {
                                adapter.addItems(reviewList)
                            }
                            binding.reviewManageRv.visibility = View.VISIBLE
                            binding.noReviewLl.visibility = View.GONE
                            binding.reviewMyreviewCountTv.text = totalElements.toString()
                        } else if (page == 0) {
                            // 첫 페이지부터 빈 경우
                            binding.reviewManageRv.visibility = View.GONE
                            binding.noReviewLl.visibility = View.VISIBLE
                            binding.reviewMyreviewCountTv.text = "0"
                        }
                        Log.d("REVIEW_API", "불러온 리뷰 수: ${reviewList.size}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("REVIEW_API", "실패 코드: ${response.code()}, 메시지: $errorBody")
                    Log.e("리뷰 불러오기 실패", "코드: ${response.code()}, 메시지: ${response.errorBody()?.string()}")
                    val rawResponse = response.raw().toString()
                    val responseText = response.errorBody()?.string()
                    Log.e("REVIEW_API_RAW", "raw=${rawResponse}")
                    Log.e("REVIEW_API_RAW_BODY", "text=$responseText")
                }
            } catch (e: Exception) {
                Log.e("REVIEW_API", "예외 발생: ${e.message}", e)
                Toast.makeText(requireContext(), "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            finally {
                isLoading = false
            }
        }
    }

    private fun showUndoSnackbar(rootView: View, message: String, onUndo: () -> Unit, onDismissed: () -> Unit, anchorViewId: Int? = null) {
        val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view as ViewGroup
        snackbarView.background = ContextCompat.getDrawable(rootView.context, R.drawable.round_style_black)

        val defaultTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        defaultTextView.visibility = View.INVISIBLE

        val customBinding = CustomSnackbarBinding.inflate(LayoutInflater.from(rootView.context), snackbarView, false)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customBinding.snackbarTextLikedTv.text = message

        var isUndoClicked = false
        customBinding.snackbarActionCancelTv.setOnClickListener {
            isUndoClicked = true
            onUndo.invoke() // '실행 취소' 시 실행될 로직
            snackbar.dismiss()
        }

        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                // '실행 취소'가 아닌 다른 이유로 스낵바가 사라지면 onDismissed 로직 실행
                if (!isUndoClicked) {
                    onDismissed.invoke()
                }
            }
        })

        snackbarView.addView(customBinding.root, 0, layoutParams)
         activity?.findViewById<View>(R.id.bottom_nav_view)?.let { snackbar.setAnchorView(it) }
        snackbar.show()
    }

    fun deleteReview(reviewId: Long, position: Int) {
        // UI에서 되돌릴 수 있도록 삭제할 아이템을 임시 저장
        val reviewToUndo = adapter.getItem(position) ?: return

        // 서버에 바로 요청하지 않고, UI에서 먼저 아이템을 제거
        adapter.removeItem(position)
        val newCount = adapter.itemCount
        binding.reviewMyreviewCountTv.text = newCount.toString()

        // '실행 취소' 기능이 있는 커스텀 스낵바
        showUndoSnackbar(binding.root, "리뷰를 삭제했어요.",
            onUndo = {
                // '실행 취소'를 누르면, UI에 아이템을 다시 추가
                adapter.addItem(position, reviewToUndo)
                binding.reviewMyreviewCountTv.text = adapter.itemCount.toString()
            },
            onDismissed = {
                // 스낵바가 그냥 사라지면 (시간 초과 등), 서버에 실제 삭제 요청
                performDeleteReviewApiCall(reviewId, position, reviewToUndo)
            }, R.id.bottom_nav_view)
    }

    // 실제 서버에 삭제 요청을 보내는 함수
    private fun performDeleteReviewApiCall(reviewId: Long, position: Int, deletedReview: MyReview) {
        lifecycleScope.launch {
            try {
                val resp = ApiClient.reviewService.deleteReview(reviewId)
                if (resp.isSuccessful) {
                    Log.d("ReviewManage", "리뷰 (id: $reviewId)가 서버에서 성공적으로 삭제되었습니다.")
                } else {
                    // 서버 삭제에 실패하면 사용자에게 알리고, UI를 원상복구
                    Toast.makeText(requireContext(), "삭제 실패: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    adapter.addItem(position, deletedReview) // UI에 아이템 복구
                    binding.reviewMyreviewCountTv.text = adapter.itemCount.toString()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                adapter.addItem(position, deletedReview) // UI에 아이템 복구
                binding.reviewMyreviewCountTv.text = adapter.itemCount.toString()
            }
        }
    }

    fun updateReview(reviewId: Long, updatedRequest: ReviewRequest2, position: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.reviewService.patchReview(reviewId, updatedRequest)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "리뷰 수정 성공!", Toast.LENGTH_SHORT).show()
                    // UI 업데이트
                    adapter.updateItem(position, updatedRequest)
                } else {
                    Toast.makeText(requireContext(), "리뷰 수정 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}