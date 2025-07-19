package com.example.sumte

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.sumte.databinding.DialogReviewDeleteAskBinding


class ReviewDeleteAskDialog ( private val onConfirm: () -> Unit) : DialogFragment(){
    private var binding: DialogReviewDeleteAskBinding?=null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        binding = DialogReviewDeleteAskBinding.inflate(layoutInflater)
        dialog.setContentView(binding!!.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding!!.reviewDeleteCancelTv.setOnClickListener { dismiss() }
        binding!!.reviewDeleteConfirmTv.setOnClickListener {
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