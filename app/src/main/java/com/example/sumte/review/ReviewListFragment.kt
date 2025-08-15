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
import com.example.sumte.ApiClient
import com.example.sumte.RetrofitClient
import com.example.sumte.SharedPreferencesManager
import com.example.sumte.databinding.FragmentReviewListBinding
import kotlinx.coroutines.launch

class ReviewListFragment : Fragment() {

    private lateinit var binding: FragmentReviewListBinding
    private lateinit var reviewAdapter: ReviewAdapter

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentReviewListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewAdapter = ReviewAdapter(emptyList())
        binding.reviewListRv.adapter = reviewAdapter
        binding.reviewListRv.layoutManager = LinearLayoutManager(requireContext())

        getReviewsFromServer()
    }

    override fun onResume() {
        super.onResume()
        Log.d("ReviewManage", "onResume() called. Reloading reviews.")
        currentPage = 0
        isLastPage = false
//        loadUserReviews(currentPage)
    }

    private fun getReviewsFromServer() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllReviews(page = 0, size = 20)
                if (response.isSuccessful) {
                    val reviewList = response.body()?.content ?: emptyList()
//                    reviewAdapter = ReviewAdapter(reviewList)
//                    binding.reviewListRv.adapter = reviewAdapter
                } else {
                    Log.e("ReviewList", "응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ReviewList", "오류 발생", e)
            }
        }
    }

    // ReviewManage 참고해서 작성
//    fun loadUserReviews(page: Int) {
//        isLoading = true
//
//        val token = SharedPreferencesManager.authToken
//        Log.d("REVIEW_List_API", "access_token=$token")
//        Log.d("REVIEW_List_API", "요청 page=$page, size=10, sort=createdAt,DESC")
//
//        lifecycleScope.launch {
//            try {
//                val response = RetrofitClient.apiService.getAllReviews(page = page)
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    Log.d("리뷰 응답", body.toString())
//                    if (body != null) {
//                        val reviewList = body.content
//                        val totalElements = body.totalElements
//                        isLastPage = body.last
//
//                        if (reviewList.isNotEmpty()) {
//                            if (page == 0) {
//                                adapter.setItems(reviewList)
//                            } else {
//                                adapter.addItems(reviewList)
//                            }
//                            binding.reviewListRv.visibility = View.VISIBLE
//                            binding.noReviewLl.visibility = View.GONE
//                            binding.reviewMyreviewCountTv.text = totalElements.toString()
//                            binding.reviewListCountTv.text=total
//                            binding.reviewListScoreTv.text= body.score.toString()
//                        } else if (page == 0) {
//                            // 첫 페이지부터 빈 경우
//                            binding.reviewListRv.visibility = View.GONE
//                            binding.noReviewLl.visibility = View.VISIBLE
//                            binding.reviewListScoreTv.text = "0"
//                        }
//                        Log.d("REVIEW_List_API", "불러온 리뷰 수: ${reviewList.size}")
//                    }
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    Log.e("REVIEW_List_API", "실패 코드: ${response.code()}, 메시지: $errorBody")
//                    Log.e("리뷰 불러오기 실패", "코드: ${response.code()}, 메시지: ${response.errorBody()?.string()}")
//                    val rawResponse = response.raw().toString()
//                    val responseText = response.errorBody()?.string()
//                    Log.e("REVIEW_LIST_API_RAW", "raw=${rawResponse}")
//                    Log.e("REVIEW_LIST_API_RAW_BODY", "text=$responseText")
//                }
//            } catch (e: Exception) {
//                Log.e("REVIEW_List_API", "예외 발생: ${e.message}", e)
//                Toast.makeText(requireContext(), "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//            }
//            finally {
//                isLoading = false
//            }
//        }
//    }
}
