package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.CustomSnackbarBinding
import com.example.sumte.databinding.FragmentLikeBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LikeFragment : Fragment(), LikeAdapter.OnLikeRemovedListener {
    lateinit var binding: FragmentLikeBinding
    private lateinit var viewModel: GuestHouseViewModel
    private lateinit var adapter: LikeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[GuestHouseViewModel::class.java]
        viewModel.likedList.observe(viewLifecycleOwner) { likedItems ->
            adapter = LikeAdapter(likedItems.toMutableList(), viewModel, this)
            binding.likeRv.layoutManager = LinearLayoutManager(requireContext())
            binding.likeRv.adapter = adapter
        }
    }

    override fun onLikeRemoved(guestHouse: GuestHouse) {
        val message = "찜 목록에서 삭제했어요."
//        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
//            .setAction("실행 취소") {
//                // 실행 취소 버튼을 눌렀을 때의 로직
//                // 뷰모델의 찜 목록에 다시 추가하는 로직을 구현.
//                viewModel.toggleLike(guestHouse)
//                // RecyclerView를 다시 그린다.
//                adapter.notifyDataSetChanged()
//            }
//            .show()
        showCustomSnackbar(binding.root, message, { onUndoAction(guestHouse) }, R.id.bottom_nav_view)
    }

    private fun onUndoAction(guestHouse: GuestHouse) {
        viewModel.toggleLike(guestHouse)
        adapter.notifyDataSetChanged()
    }

    private fun showCustomSnackbar(rootView: View, message: String, onAction: () -> Unit, anchorViewId: Int? = null) {
        val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view as ViewGroup
        snackbarView.background = ContextCompat.getDrawable(rootView.context, R.drawable.round_style_black)

        val defaultTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        defaultTextView?.visibility = View.INVISIBLE

        val customBinding = CustomSnackbarBinding.inflate(LayoutInflater.from(rootView.context), snackbarView, false)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        customBinding.snackbarTextLikedTv.text = message

        customBinding.snackbarActionCancelTv.setOnClickListener {
            onAction.invoke() // 전달받은 실행 취소 로직 호출
            snackbar.dismiss() // 스낵바 닫기
        }

        snackbarView.addView(customBinding.root, 0, layoutParams)

        anchorViewId?.let { id ->
            snackbar.setAnchorView(id)
        }

        snackbar.show()
    }
}