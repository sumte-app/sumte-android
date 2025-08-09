package com.example.sumte.mybook

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentBookedDetailBinding
class BookedDetailFragment : Fragment() {
    lateinit var binding: FragmentBookedDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookedDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookedData = arguments?.getParcelable<BookedData>("bookedData")
        if (bookedData == null) {
            Log.e("BookedDetailFragment", "bookedData is null!")
        } else {
            bookedData?.let {
                binding.bookedDate.text = it.bookedDate
                binding.houseName.text = it.houseName
                binding.roomType.text = it.roomType
                binding.startDate.text = it.startDate
                binding.endDate.text = it.endDate
                binding.dateCount.text = it.dateCount
                binding.adultCount.text = "${it.adultCount}명"
                binding.childCount.text = "${it.childCount}명"
                // 필요하면 더 바인딩 추가
            }
        }
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
