package com.example.sumte.search

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sumte.FilterOptions
import com.example.sumte.R
import com.example.sumte.databinding.FragmentSearchResultBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookInfoViewModel by activityViewModels()
    private var keyword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // arguments가 없거나 키워드가 없을 수도 있으므로 null 체크
        keyword = arguments?.getString("keyword")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)

        val startDate = viewModel.startDate
        val endDate = viewModel.endDate
        val nights = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)

        binding.startDate.text = startDate.format(formatter)
        binding.endDate.text = endDate.format(formatter)
        binding.dateCount.text = "${nights}박"

        binding.adultCount.text = "성인 ${viewModel.adultCount}"
        binding.childCount.text =
            if (viewModel.childCount > 0) "아동 ${viewModel.childCount}" else ""

        binding.countComma.visibility = if (viewModel.childCount > 0) View.VISIBLE else View.GONE

        if (!keyword.isNullOrBlank()) {
            // 키워드가 있을 때 처리
            binding.searchText.setText(keyword)
        }
        binding.searchText.setOnClickListener{
        }



        binding.searchResultAdjustmentsLl.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, FilteringFragment())
                .addToBackStack(null)
                .commit()
        }

        val filterOptions = requireActivity().intent.getParcelableExtra<FilterOptions>("filterOptions")
        filterOptions?.let {
            // 필터 데이터 처리 가능
        }

        binding.dateChangeBar.setOnClickListener {
            val fragment = BookInfoDateFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.countChangeBar.setOnClickListener {
            val fragment = BookInfoCountFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.backBtn.setOnClickListener {
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
