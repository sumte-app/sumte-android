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

    private var _binding: DialogPaymentProcessBinding? = null
    private val binding get() = _binding!!

    private var dotAnimator: PaymentProcessDotAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogPaymentProcessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dotAnimator = PaymentProcessDotAnimator(listOf(binding.dot1, binding.dot2, binding.dot3)).also {
            it.start()
        }
    }

    override fun onDestroyView() {
        dotAnimator?.stop()
        dotAnimator = null
        _binding = null
        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setDimAmount(0.5f)
        }
        isCancelable = false
    }
}

