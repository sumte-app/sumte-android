package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.sumte.databinding.FragmentBottomSheetReviewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PhotoOptionsBottomSheet: BottomSheetDialogFragment() {
    private var _binding: FragmentBottomSheetReviewBinding? = null
    private val binding get() = _binding!!

    interface OnOptionSelectedListener {
        fun onTakePhotoSelected()
        fun onSelectFromAlbumSelected()
    }

    private var listener: OnOptionSelectedListener? = null
    fun setOnOptionSelectedListener(listener: OnOptionSelectedListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bottomSheetCloseIv.setOnClickListener {
            dismiss() // 닫기 버튼 처리
        }

        binding.optionTakePhotoLl.setOnClickListener {
            listener?.onTakePhotoSelected()
            dismiss() // 선택 후 닫기
        }

        binding.optionSelectFromAlbumLl.setOnClickListener {
            listener?.onSelectFromAlbumSelected()
            dismiss() // 선택 후 닫기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}