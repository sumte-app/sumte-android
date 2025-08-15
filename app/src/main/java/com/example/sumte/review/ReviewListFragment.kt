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

// 리뷰 전체 조회 화면 코드
class ReviewListFragment : Fragment() {

    private lateinit var binding: FragmentReviewListBinding
    private lateinit var reviewAdapter: ReviewListAdapter
    private var guesthouseId: Long = -1L // 전달받을 guesthouseId

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 다른 프래그먼트에서 guesthouseId를 받는 부분
        // arguments가 null이 아닐 경우에만 값을 가져오도록 처리
        arguments?.let {
//            guesthouseId = it.getLong("guesthouseId_key", -1L) // "guesthouseId_key"는 전달하는 쪽에서 사용한 키와 일치해야함.
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentReviewListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        if (guesthouseId != -1L) {
            getReviewsFromServer(guesthouseId)
        } else {
            // guesthouseId가 전달되지 않은 경우의 예외 처리
            Log.e("ReviewListFragment", "Guesthouse ID is not provided.")
            // 사용자에게 알림을 보여주는 등의 처리를 할 수 있습니다.
        }
    }

    // RecyclerView 초기 설정
    private fun setupRecyclerView() {
        reviewAdapter = ReviewListAdapter(emptyList()) // 빈 리스트로 어댑터 초기화
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
                        reviewAdapter.updateData(reviewList) // 어댑터에 데이터 업데이트
                    } else {
                        // 리뷰가 없는 경우 처리
                        Log.d("ReviewListFragment", "No reviews found.")
                    }
                } else {
                    Log.e("ReviewListFragment", "API 응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ReviewListFragment", "API 호출 중 오류 발생", e)
            }
        }
    }

    // onResume은 현재 로직에서는 불필요해 보이므로 일단 비워두거나,
    // 화면에 다시 돌아왔을 때 새로고침이 필요하다면 아래와 같이 수정합니다.
    override fun onResume() {
        super.onResume()
        // 필요 시 데이터 새로고침
        // currentPage = 0
        // isLastPage = false
        // if (guesthouseId != -1L) {
        //     getReviewsFromServer(guesthouseId)
        // }
    }

}
