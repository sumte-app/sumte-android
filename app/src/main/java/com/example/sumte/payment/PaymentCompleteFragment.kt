package com.example.sumte.payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sumte.R




class PaymentCompleteFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val amount = requireArguments().getInt("amount", 0)
        val method = requireArguments().getString("method").orEmpty()
        val paymentId = requireArguments().getString("paymentId").orEmpty()
        val tid = requireArguments().getString("tid").orEmpty()

    }
}