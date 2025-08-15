package com.example.sumte.payment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.example.sumte.R
import com.example.sumte.databinding.FragmentPaymentCompleteBinding


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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val amount = requireArguments().getInt("amount", 0)
        val method = requireArguments().getString("method").orEmpty()
        val paymentId = requireArguments().getString("paymentId").orEmpty()
        val tid = requireArguments().getString("tid").orEmpty()

    }


}