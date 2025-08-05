package com.example.sumte.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentSearchBinding
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding

    val seoulZone = ZoneId.of("Asia/Seoul")
    private var startDate: LocalDate? = LocalDate.now(seoulZone)
    private var endDate: LocalDate? = LocalDate.now(seoulZone).plusDays(1)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)

        startDate?.let { start ->
            endDate?.let { end ->
                val nights = ChronoUnit.DAYS.between(start, end)

                binding.startDate.text = start.format(formatter)
                binding.endDate.text = end.format(formatter)
                binding.dateCount.text = "${nights}박"
            }
        }
        binding.bookInfo.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java)
            startActivity(intent)
        }

        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}