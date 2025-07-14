package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.FragmentReviewManageBinding
import kotlinx.coroutines.launch

class ReviewManage: Fragment() {
    private var _binding: FragmentReviewManageBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy{ReviewManageAdapter(this)}

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
        binding.reviewManageRv.adapter = adapter
        binding.reviewManageRv.layoutManager = LinearLayoutManager(requireContext())
        loadUserReviews()
    }

    fun loadUserReviews() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.reviewService.getMyReviews()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val reviewList = body.content
                        val totalCount = body.totalElements

                        binding.reviewMyreviewCountTv.text = totalCount.toString()
                        // 어댑터에 데이터 전달
//                        val adapter = ReviewManageAdapter()
//                        binding.reviewManageRv.adapter = adapter
//                        binding.reviewManageRv.layoutManager = LinearLayoutManager(requireContext())
                        adapter.setItems(reviewList)
                    }
                } else {
                    Toast.makeText(requireContext(), "리뷰 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), "리뷰가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(),
                        "삭제 실패: ${resp.code()}",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    "네트워크 오류: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}