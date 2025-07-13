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
    private val adapter by lazy{ReviewManageAdapter()}

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

    private fun loadUserReviews(page: Int = 0) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.reviewService.getMyReviews(page = page)
                if (response.isSuccessful) {
                    val body = response.body() ?: return@launch
                    adapter.submitList(body.content)

                } else {
                    Toast.makeText(requireContext(),
                        "불러오기 실패: ${response.code()}",
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