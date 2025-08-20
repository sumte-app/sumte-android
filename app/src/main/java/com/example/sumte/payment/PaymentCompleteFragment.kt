package com.example.sumte.payment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.example.sumte.HomeFragment
import com.example.sumte.R
import com.example.sumte.databinding.FragmentPaymentCompleteBinding
import com.example.sumte.housedetail.HouseDetailFragment
import com.example.sumte.payment.PaymentExtras.EXTRA_AMOUNT
import com.example.sumte.payment.PaymentExtras.EXTRA_CREATED_AT
import com.example.sumte.payment.PaymentExtras.EXTRA_GUESTHOUSE_NAME
import com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_ID
import com.example.sumte.payment.PaymentExtras.EXTRA_ROOM_NAME


class PaymentCompleteFragment : Fragment() {
    private lateinit var binding : FragmentPaymentCompleteBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentCompleteBinding.inflate(inflater, container, false)

        binding.homeBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val gh       = arguments?.getString(EXTRA_GUESTHOUSE_NAME)
        val room     = arguments?.getString(EXTRA_ROOM_NAME)
        val amount   = arguments?.getInt(EXTRA_AMOUNT)
        val created  = arguments?.getString(EXTRA_CREATED_AT)

        val prettyAmount = java.text.NumberFormat
            .getInstance(java.util.Locale.KOREA)
            .format(amount) + "원"

        binding.bookedName.text = gh
        binding.roomType.text = room
        binding.price.text = prettyAmount
        binding.cancelTime.text = created


    }




}