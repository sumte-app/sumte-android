package com.example.sumte.mybook

import BookedListMainFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.example.sumte.R
import com.example.sumte.databinding.FragmentBookedCancelBinding
import java.text.SimpleDateFormat
import java.util.*

class BookedCancelFragment : Fragment() {

    private lateinit var binding: FragmentBookedCancelBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookedCancelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments
        val guestHouseName = args?.getString("guestHouseName") ?: ""
        val roomName = args?.getString("roomName") ?: ""
        val startDate = args?.getString("startDate") ?: ""
        val endDate = args?.getString("endDate") ?: ""
        val nightCount = args?.getString("nightCount") ?: ""
        val adultCount = args?.getString("adultCount") ?: ""
        val childCount = args?.getString("childCount") ?: ""
        val totalPrice = args?.getString("totalPrice") ?: ""

        // 현재 시간 취소일시
        val currentTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy.MM.dd(E) HH:mm", Locale.KOREA)
        binding.cancelTime.text = formatter.format(currentTime)
        binding.bookedName.text = guestHouseName
        binding.roomType.text = roomName
        binding.startDate.text = startDate
        binding.endDate.text = endDate
        binding.dateCount.text = nightCount
        binding.adultCount.text = adultCount
        binding.childCount.text = childCount
        binding.price.text = totalPrice


        //예약내역으로 이동
        binding.bookedListBtn.setOnClickListener {
            val fragment = BookedListMainFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.booked_list_container, fragment)
                //이 밑에 두개는 필요없을거같긴한데
                .addToBackStack(null) // 이전 Fragment로 돌아갈 수 있게 추가
                .commit()
        }

        //홈 이동
        binding.homeBtn.setOnClickListener {
            //안중요 나중생각
        }
        //뒤로가기 시 상세예약내역
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
