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
import java.text.DecimalFormat

// 리뷰 전체 조회 화면 코드
class ReviewListFragment : Fragment() {
    private var _binding: FragmentReviewListBinding ?= null
    private val binding get() = _binding!!
    private lateinit var reviewAdapter: ReviewListAdapter
    private var guesthouseId: Long = -1L
    private var averageScore: Double = 0.0
    private var reviewCount: Int = 0

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // HouseDetailFragment로부터 guesthouseId를 받는 부분
        arguments?.let {
            guesthouseId = it.getLong("guesthouseId_key", -1L)
            averageScore = it.getDouble("averageScore_key", 0.0)
            reviewCount = it.getInt("reviewCount_key", 0)

            Log.d("DEBUG_ReviewList", "Bundle에서 받은 averageScore 값: $averageScore")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기 버튼
        binding.reviewListArrowIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupRecyclerView()

        if (guesthouseId != -1L) {
            getReviewsFromServer(guesthouseId)
        } else {
            // guesthouseId가 전달되지 않은 경우의 예외 처리
            Log.e("ReviewListFragment", "Guesthouse ID is not provided.")
            Toast.makeText(requireContext(), "게스트하우스 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
        val decimalFormat = DecimalFormat("#.#")
        binding.reviewListScoreTv.text = decimalFormat.format(averageScore)
        binding.reviewListCountTv.text=reviewCount.toString()
    }

    // RecyclerView 초기 설정
    private fun setupRecyclerView() {
        reviewAdapter = ReviewListAdapter(emptyList(), this) // 빈 리스트로 어댑터 초기화
        binding.reviewListRv.adapter = reviewAdapter
        binding.reviewListRv.layoutManager = LinearLayoutManager(requireContext())

        // TODO: 무한 스크롤 구현 시 여기에 OnScrollListener 추가
    }

    // API를 호출하여 리뷰 데이터를 가져오는 함수
    private fun getReviewsFromServer(guesthouseId: Long) {
        lifecycleScope.launch {
            try {
                // 수정한 API 호출
                val response = ApiClient.reviewService.getGuesthouseReviews(
                    guesthouseId = guesthouseId,
                    page = currentPage,
                    size = 10
                )

                if (response.isSuccessful) {
                    // 응답 본문에서 리뷰 목록(content)을 가져옴
                    val reviewList = response.body()?.content ?: emptyList()
                    if (reviewList.isNotEmpty()) {
                        binding.noReviewLl.visibility=View.GONE
                        binding.reviewListRv.visibility=View.VISIBLE
                        reviewAdapter.updateData(reviewList) // 어댑터에 데이터 업데이트
                    } else {
                        // 리뷰가 없는 경우 처리
                        Log.d("ReviewListFragment", "No reviews found.")
                        binding.noReviewLl.visibility=View.VISIBLE
                        binding.reviewListRv.visibility=View.GONE
                    }
                } else {
                    Log.e("ReviewListFragment", "API 응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ReviewListFragment", "API 호출 중 오류 발생", e)
            }
        }
    }

    // 화면에 다시 돌아왔을 때 새로고침
    override fun onResume() {
        super.onResume()
         currentPage = 0
         isLastPage = false
         if (guesthouseId != -1L) {
             getReviewsFromServer(guesthouseId)
         }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
