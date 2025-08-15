package com.example.sumte.review

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.sumte.databinding.DialogReviewDeleteAskBinding


class ReviewDeleteAskDialog ( private val onConfirm: () -> Unit) : DialogFragment(){
    private var binding: DialogReviewDeleteAskBinding?=null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        binding = DialogReviewDeleteAskBinding.inflate(layoutInflater)
        dialog.setContentView(binding!!.root)

        val widthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            282f,
            resources.displayMetrics
        ).toInt()

        dialog.window?.setLayout(
            widthPx,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

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