package com.example.sumte.review

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.ApiClient
import com.example.sumte.SharedPreferencesManager
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

    fun deleteReview(reviewId: Long, position: Int) {
        lifecycleScope.launch {
            try {
                val resp = ApiClient.reviewService.deleteReview(reviewId)
                if (resp.isSuccessful) {
                    // 로컬 리스트에서 제거
                    adapter.removeItem(position)
                    // 총 개수 텍스트뷰 갱신
                    val newCount = adapter.itemCount
                    binding.reviewMyreviewCountTv.text = newCount.toString()

                    Snackbar.make(binding.root, "삭제가 완료되었습니다.", Snackbar.LENGTH_LONG)
                        .setAction("실행 취소") {
                            // 실행 취소 버튼을 눌렀을 때 실행될 로직을 작성
                            Toast.makeText(requireContext(), "삭제가 취소되었습니다", Toast.LENGTH_SHORT).show()
                        }
                        .show()
                } else {
                    Toast.makeText(requireContext(), "삭제 실패: ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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