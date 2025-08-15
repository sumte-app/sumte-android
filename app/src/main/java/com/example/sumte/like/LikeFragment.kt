package com.example.sumte.like

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.ApiClient
import com.example.sumte.GuesthouseSummaryDto
import com.example.sumte.R
import com.example.sumte.databinding.CustomSnackbarBinding
import com.example.sumte.databinding.FragmentLikeBinding
import com.example.sumte.guesthouse.GuestHouseViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LikeFragment : Fragment(), LikeAdapter.OnLikeRemovedListener {
    private lateinit var binding: FragmentLikeBinding
    private lateinit var adapter: LikeAdapter
    private lateinit var viewModel: GuestHouseViewModel
    private val likeService = ApiClient.likeService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = LikeAdapter(mutableListOf(), this)
        binding.likeRv.layoutManager = LinearLayoutManager(requireContext())
        binding.likeRv.adapter = adapter
        viewModel = ViewModelProvider(requireActivity())[GuestHouseViewModel::class.java]

        // ViewModel의 StateFlow를 구독하여 UI 자동 업데이트
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.likedGuesthouses.collect { likedList ->
                adapter.setItems(likedList)
            }
        }
//        loadFavorites()
    }

    // 화면이 다시 보일 때마다 최신 찜 목록을 불러오도록
    override fun onResume() {
        super.onResume()
        viewModel.loadLikedGuesthouses()
    }

//    private fun loadFavorites() {
//        lifecycleScope.launch {
//            try {
//                val response = likeService.getLikes()
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    body?.let {
//                        adapter.setItems(it.content)
//                    }
//                } else {
//                    Log.e("LikeFragment", "Failed: ${response.code()}")
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

    override fun onLikeRemoved(guestHouse: GuesthouseSummaryDto) {
        viewModel.removeLike(guestHouse.id)

        showCustomSnackbar(binding.root, "찜 목록에서 삭제했어요.", {
            // '실행 취소' 기능도 ViewModel을 통해 구현해야 합니다. (아래 2번 참고)
            viewModel.addLike(guestHouse.id) // 예시
        }, R.id.bottom_nav_view)
    }

    private fun showCustomSnackbar(rootView: View, message: String, onAction: () -> Unit, anchorViewId: Int? = null) {
        val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view as ViewGroup
        snackbarView.background = ContextCompat.getDrawable(rootView.context,
            R.drawable.round_style_black
        )

        val defaultTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        defaultTextView?.visibility = View.INVISIBLE

        val customBinding = CustomSnackbarBinding.inflate(LayoutInflater.from(rootView.context), snackbarView, false)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        customBinding.snackbarTextLikedTv.text = message
        customBinding.snackbarActionCancelTv.setOnClickListener {
            onAction.invoke()
            snackbar.dismiss()
        }

        snackbarView.addView(customBinding.root, 0, layoutParams)
        anchorViewId?.let { snackbar.setAnchorView(it) }
        snackbar.show()
    }
}
