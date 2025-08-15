package com.example.sumte.mybook

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sumte.databinding.FragmentBookedDetailBinding
import com.example.sumte.reservation.ReservationRepository
import kotlinx.coroutines.launch

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
        val reservationId = arguments?.getInt("reservationId") ?: -1
        Log.d("reservationIdFromMain", "get id : ${reservationId}")
        if (reservationId == -1) {
            Log.e("BookedDetailFragment", "reservationId is missing!")
            return
        }
        //사용자정보 없이 조회 가능
        val repository = ReservationRepository(requireContext())

        // CoroutineScope를 사용해 API 호출
        viewLifecycleOwner.lifecycleScope.launch {
            val detail = repository.getReservationDetail(reservationId)
            if (detail != null) {
                binding.bookedDate.text = detail.reservedAt //formatter수정
                binding.houseName.text = detail.guestHouseName
                binding.roomType.text = detail.roomName
                binding.startDate.text = detail.startDate
                binding.endDate.text = detail.endDate
                binding.price.text = "${detail.totalPrice}원"
                binding.dateCount.text = "${detail.nightCount}박"
                binding.adultCount.text = "${detail.adultCount}명"
                binding.childCount.text = if (detail.childCount > 0) "${detail.childCount}명" else ""
            } else {
                Log.e("BookedDetailFragment", "Failed to fetch reservation detail")
            }
        }

//        bundle버전
//        val bookedData = arguments?.getParcelable<BookedData>("bookedData")
//        if (bookedData == null) {
//            Log.e("BookedDetailFragment", "bookedData is null!")
//        } else {
//            bookedData?.let {
//                binding.bookedDate.text = it.bookedDate
//                binding.houseName.text = it.houseName
//                binding.roomType.text = it.roomType
//                binding.startDate.text = it.startDate
//                binding.endDate.text = it.endDate
//                binding.dateCount.text = it.dateCount
//                binding.adultCount.text = "${it.adultCount}명"
//                binding.childCount.text = "${it.childCount}명"
//                // 필요하면 더 바인딩 추가
//            }
//        }
        binding.cancelBtn.setOnClickListener {
            binding.popupOverlay.visibility = View.VISIBLE
        }
        binding.noBtn.setOnClickListener {
            binding.popupOverlay.visibility = View.GONE
        }
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
