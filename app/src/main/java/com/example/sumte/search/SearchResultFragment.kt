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
import androidx.lifecycle.ViewModelProvider
import com.example.sumte.App
import com.example.sumte.FilterOptions
import com.example.sumte.R
import com.example.sumte.databinding.FragmentSearchResultBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!
    //private val viewModel: BookInfoViewModel by activityViewModels()
    private val viewModel by lazy {
        ViewModelProvider(
            App.instance,
            ViewModelProvider.AndroidViewModelFactory.getInstance(App.instance)
        )[BookInfoViewModel::class.java]
    }
    private var keyword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            keyword = bundle.getString("keyword")

            // 전달받은 날짜 문자열
            val startDateStr = bundle.getString("startDate")
            val endDateStr = bundle.getString("endDate")

            // 전달받은 인원 정보 (기본값은 기존 뷰모델값)
            val adultCount = bundle.getInt("adultCount", viewModel.adultCount)
            val childCount = bundle.getInt("childCount", viewModel.childCount)

            // 날짜 문자열이 null이 아니면 LocalDate로 변환 후 뷰모델에 저장
            if (startDateStr != null && endDateStr != null) {
                // 예: "8.15 토" 이런 포맷이라면 파싱이 필요함
                val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)
                try {
                    val startDateParsed = java.time.LocalDate.parse(startDateStr, formatter)
                    val endDateParsed = java.time.LocalDate.parse(endDateStr, formatter)

                    viewModel.startDate = startDateParsed
                    viewModel.endDate = endDateParsed
                } catch (e: Exception) {
                    // 포맷이 다르면 예외 처리 or 로그 출력
                    e.printStackTrace()
                }
            }

            // 뷰모델 인원 업데이트
            viewModel.adultCount = adultCount
            viewModel.childCount = childCount
        }
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
            val fragment = SearchFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()

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
        binding.searchText.setOnClickListener {
            val fragment = SearchFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.backBtn.setOnClickListener {
            val fragment = SearchFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
