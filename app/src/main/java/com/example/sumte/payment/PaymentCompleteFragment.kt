package com.example.sumte.payment

import BookedListMainFragment
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.bumptech.glide.Glide
import com.example.sumte.HomeFragment
import com.example.sumte.MainActivity
import com.example.sumte.R
import com.example.sumte.common.getBookInfoViewModel
import com.example.sumte.databinding.FragmentPaymentCompleteBinding
import com.example.sumte.housedetail.HouseDetailFragment
import com.example.sumte.mybook.BookedListActivity
import com.example.sumte.payment.PaymentExtras.EXTRA_AMOUNT
import com.example.sumte.payment.PaymentExtras.EXTRA_CREATED_AT
import com.example.sumte.payment.PaymentExtras.EXTRA_GUESTHOUSE_NAME
import com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_ID
import com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_NAME
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


class PaymentCompleteFragment : Fragment() {
    private lateinit var binding : FragmentPaymentCompleteBinding

    private val vm by lazy { getBookInfoViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentCompleteBinding.inflate(inflater, container, false)

        binding.listBtn.setOnClickListener{
            val intent = Intent(requireContext(), BookedListActivity::class.java)
            startActivity(intent)
        }

        binding.homeBtn.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish() // 현재 액티비티 종료
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gh       = arguments?.getString(EXTRA_GUESTHOUSE_NAME)
        val room     = arguments?.getString(EXTRA_ROOM_NAME)
        val amount   = arguments?.getInt(EXTRA_AMOUNT)
        val created  = arguments?.getString(EXTRA_CREATED_AT)

        val prettyAmount = java.text.NumberFormat
            .getInstance(java.util.Locale.KOREA)
            .format(amount) + "원"

        val seoul = ZoneId.of("Asia/Seoul")
        val fmt = DateTimeFormatter.ofPattern("M.d E", Locale.KOREAN)

        val start = vm.startDate ?: LocalDate.now(seoul)
        val end = vm.endDate ?: LocalDate.now(seoul).plusDays(1)
        val nights = ChronoUnit.DAYS.between(start, end)

        binding.bookedName.text = gh
        binding.roomType.text = room
        binding.price.text = prettyAmount
        binding.cancelTime.text = created

        binding.startDate.text = start.format(fmt)
        binding.endDate.text = end.format(fmt)
        binding.dateCount.text = "${nights}박"


        Glide.with(this)
            .load(vm.roomImageUrl)
            .placeholder(R.drawable.sample_room1)
            .error(R.drawable.sample_room1)
            .centerCrop()
            .into(binding.detailImg)

    }

    override fun onResume() {
        super.onResume()
        val window = requireActivity().window

        // 상태바 배경색
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(
            requireContext(), R.color.white
        )

        // 상태바 아이콘 색(밝은 배경이면 true)
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true
    }




}