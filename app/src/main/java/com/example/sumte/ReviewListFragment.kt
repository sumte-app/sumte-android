package com.example.sumte

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.FragmentReviewListBinding
import kotlinx.coroutines.launch

class ReviewListFragment : Fragment() {

    private lateinit var binding: FragmentReviewListBinding
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentReviewListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewAdapter = ReviewAdapter(emptyList())
        binding.reviewRecyclerView.adapter = reviewAdapter
        binding.reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        getReviewsFromServer()
    }

    private fun getReviewsFromServer() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllReviews(page = 0, size = 20)
                if (response.isSuccessful) {
                    val reviewList = response.body()?.content ?: emptyList()
                    reviewAdapter = ReviewAdapter(reviewList)
                    binding.reviewRecyclerView.adapter = reviewAdapter
                } else {
                    Log.e("ReviewList", "응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ReviewList", "오류 발생", e)
            }
        }
    }
}
