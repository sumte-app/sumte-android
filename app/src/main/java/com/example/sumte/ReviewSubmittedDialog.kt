package com.example.sumte

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.sumte.databinding.DialogReviewSubmittedBinding

class ReviewSubmittedDialog ( private val onConfirm: () -> Unit) : DialogFragment(){
    private var binding: DialogReviewSubmittedBinding?=null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        binding = DialogReviewSubmittedBinding.inflate(layoutInflater)
        dialog.setContentView(binding!!.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding!!.reviewSubmittedApplyTv.setOnClickListener {
            onConfirm()
            dismiss()
        }
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding=null
    }
}