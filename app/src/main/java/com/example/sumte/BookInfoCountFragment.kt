package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.replace
import com.example.sumte.databinding.FragmentBookInfoCountBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class BookInfoCountFragment : Fragment() {
    lateinit var binding: FragmentBookInfoCountBinding
    private val viewModel: BookInfoViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBookInfoCountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val formatter = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)

        val start = viewModel.startDate
        val end = viewModel.endDate
        val nights = java.time.temporal.ChronoUnit.DAYS.between(start, end)

        binding.startDate.text = start.format(formatter)
        binding.endDate.text = "${end.format(formatter)}"
        binding.dateCount.text = ", ${nights}박"

        var adultCount = viewModel.adultCount
        var childCount = viewModel.childCount

        binding.adultCount.text = adultCount.toString()
        binding.childCount.text = childCount.toString()
        binding.adultCountText.text = "성인 $adultCount"
        binding.childCountText.text = if (childCount > 0) ", 아동 $childCount" else ""


        binding.adultPlusBtn.setOnClickListener {
            adultCount++
            viewModel.adultCount = adultCount
            if (adultCount > 1 ){
                binding.adultMinusBtn.setImageResource(R.drawable.minus_green)
            }
            binding.adultCount.text = adultCount.toString()
            binding.adultCountText.text = String.format("성인 %d", adultCount)
        }
        binding.adultMinusBtn.setOnClickListener {
            if (adultCount > 1) {
                adultCount--
                binding.adultCount.text = adultCount.toString()
            }
            if (adultCount == 1) {
                binding.adultMinusBtn.setImageResource(R.drawable.minus_gray)
            }
            viewModel.adultCount = adultCount
            binding.adultCountText.text = String.format("성인 %d", adultCount)
        }
        binding.childPlusBtn.setOnClickListener {
            childCount++
            viewModel.childCount = childCount
            if (childCount > 0) {
                binding.childMinusBtn.setImageResource(R.drawable.minus_green)
            }
            binding.childCount.text = childCount.toString()
            binding.childCountText.text = String.format(", 아동 %d", childCount)
        }
        binding.childMinusBtn.setOnClickListener {
            if (childCount > 0) {
                childCount--
                binding.childCount.text = childCount.toString()
                binding.childCountText.text = String.format(", 아동 %d", childCount)
            }
            if (childCount == 0){
                binding.childMinusBtn.setImageResource(R.drawable.minus_gray)
                binding.childCountText.text = null
            }
            viewModel.childCount = childCount
        }

        binding.applyBtn.setOnClickListener {
            val intent = Intent(requireContext(), SearchResultActivity::class.java)
            startActivity(intent)
        }
        binding.calendar.setOnClickListener {
            val fragment = BookInfoDateFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }

    }

}