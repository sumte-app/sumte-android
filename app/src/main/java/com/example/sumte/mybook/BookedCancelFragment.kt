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

        // 현재 시간 취소일시
        val currentTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy.MM.dd(E) HH:mm", Locale.KOREA)
        binding.cancelTime.text = formatter.format(currentTime)

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
