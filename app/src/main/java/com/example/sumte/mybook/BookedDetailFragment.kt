package com.example.sumte.mybook

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sumte.R
import com.example.sumte.databinding.FragmentBookedDetailBinding
import com.example.sumte.reservation.ReservationRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        fun formatReservedAt(reservedAt: String): String {
            val cleanString = reservedAt.substring(0, 19)
            val ldt = LocalDateTime.parse(cleanString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E) HH:mm", Locale.KOREAN)
            return ldt.format(formatter)
        }
        // 데이터 bind
        viewLifecycleOwner.lifecycleScope.launch {
            val detail = repository.getReservationDetail(reservationId)
            if (detail != null) {
                binding.bookedDate.text = formatReservedAt(detail.reservedAt) //formatter수정
                binding.houseName.text = detail.guestHouseName
                binding.roomType.text = detail.roomName
                binding.startDate.text = detail.startDate
                binding.endDate.text = detail.endDate
                binding.price.text = "${detail.totalPrice}원"
                binding.dateCount.text = "${detail.nightCount}박"
                binding.adultCount.text = "${detail.adultCount}명"
                binding.childCount.text = if (detail.childCount > 0) "${detail.childCount}명" else ""

                //취소일 때
                if (detail.status == "CANCELED") {
                    binding.statusCancel.visibility = View.VISIBLE
                    binding.cancelBtn.isEnabled = false
                    binding.cancelBtn.setBackgroundResource(R.drawable.apply_btn_disabled_style)

                    val dimAlpha = 0.5f
                    binding.detailImg.alpha = dimAlpha
                    binding.houseName.alpha = dimAlpha
                    binding.roomType.alpha = dimAlpha
                    binding.selectedDate.alpha = dimAlpha
                    binding.selectedCount.alpha = dimAlpha
                }
            } else {
                Log.e("BookedDetailFragment", "Failed to fetch reservation detail")
            }
        }
        binding.cancelBtn.setOnClickListener {
            binding.popupOverlay.visibility = View.VISIBLE
        }
        //예약취소 api호출
        binding.yesBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val response = repository.cancelReservation(reservationId)

                if (response?.success == true) {
                    Toast.makeText(requireContext(), "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    binding.popupOverlay.visibility = View.GONE
                    // 취소완료 화면으로 이동
                    val fragment = BookedCancelFragment()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.booked_list_container, fragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "예약 취소 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //예약유지버튼
        binding.noBtn.setOnClickListener {
            binding.popupOverlay.visibility = View.GONE
        }

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
