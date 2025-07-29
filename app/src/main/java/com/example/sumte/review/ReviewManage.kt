package com.example.sumte.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.ApiClient
import com.example.sumte.databinding.FragmentReviewManageBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ReviewManage: Fragment() {
    private var _binding: FragmentReviewManageBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy{ ReviewManageAdapter(this) }

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
                        val totalElements = body.totalElements

                        if (totalElements != 0) {
                            // 리뷰가 있을 때
                            binding.reviewManageRv.visibility = View.VISIBLE
                            binding.noReviewLl.visibility = View.GONE
                            binding.reviewMyreviewCountTv.text = totalElements.toString()

                            // 리뷸 어뎁터 통해서 화면에 띄우는 기능 추가해야함

                            adapter.setItems(reviewList)
                        } else {
                            // 리뷰가 없을 때 (기본 레이아웃 상태 유지)
                            binding.reviewManageRv.visibility = View.GONE
                            binding.noReviewLl.visibility = View.VISIBLE
                        }
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

                    Snackbar.make(binding.root, "삭제가 완료되었습니다.", Snackbar.LENGTH_LONG) // 메시지가 긴 경우 LENGTH_LONG 사용
                        .setAction("실행 취소") {
                            // 실행 취소 버튼을 눌렀을 때 실행될 로직을 작성
                            Toast.makeText(requireContext(), "삭제가 취소되었습니다", Toast.LENGTH_SHORT).show()
                        }
                        .show()
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