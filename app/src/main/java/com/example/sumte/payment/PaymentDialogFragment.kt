package com.example.sumte.payment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.sumte.R
import com.example.sumte.databinding.DialogPaymentProcessBinding


class PaymentDialogFragment : DialogFragment() {

    private lateinit var binding: DialogPaymentProcessBinding

    private lateinit var dotAnimator: PaymentProcessDotAnimator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogPaymentProcessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dotAnimator = PaymentProcessDotAnimator(
            listOf(binding.dot1, binding.dot2, binding.dot3)
        )
        dotAnimator.start()
    }

    override fun onDestroyView() {
        dotAnimator.stop()

        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }
}
