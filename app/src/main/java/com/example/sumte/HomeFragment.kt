package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.ImageUpload.ImageUploadActivity
import com.example.sumte.databinding.FragmentHomeBinding
import com.example.sumte.guesthouse.GuestHouseAdapter
import com.example.sumte.guesthouse.GuestHouseViewModel
import com.example.sumte.housedetail.HouseDetailFragment
import com.example.sumte.review.ReviewWriteActivity
import com.example.sumte.search.BookInfoActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: GuestHouseViewModel
    private lateinit var adapter: GuestHouseAdapter

    // UI 페이지는 1부터(서버는 0부터라 ViewModel에서 보정)
    private var page = 1
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false

    // 로딩 오버레이 점 애니메이션
    private var dotJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.searchBoxLl.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java)
            intent.putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, "search")
            startActivity(intent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[GuestHouseViewModel::class.java]

        adapter = GuestHouseAdapter(
            viewModel = viewModel,
            onItemClick = { guestHouse ->
                val id = guestHouse.id
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, HouseDetailFragment.newInstance(id.toInt()))
                    .addToBackStack(null)
                    .commit()
            }
        )

        val lm = LinearLayoutManager(requireContext())
        binding.guesthouseRv.layoutManager = lm
        binding.guesthouseRv.adapter = adapter

        // 캐시가 있으면 즉시 복원(네트워크 X)
        if (viewModel.items.isNotEmpty()) {
            adapter.replaceAll(viewModel.items)
            page = viewModel.nextPage
            isLastPage = viewModel.isLastPageCached
            showLoading(false) // 캐시가 있으니 오버레이 꺼둠

            // 화면에 다시 돌아왔을 때를 대비해 찜 상태만 새로고침
            viewLifecycleOwner.lifecycleScope.launch {
                // ViewModel에 이미 로드된 아이템들의 찜 정보만 다시 확인
                viewModel.updateLikedStatusForVisibleItems()
                // 찜 상태가 변경되었을 수 있으므로 어댑터에 알려줌
                adapter.notifyDataSetChanged()
            }
        } else {
            // 초기 진입: 홈 UI 나오기 전에 로딩 오버레이 켠다
            showLoading(true)
            viewLifecycleOwner.lifecycleScope.launch {
                // initialLikesLoaded가 true가 될 때까지 기다렸다가 한 번만 실행
//                viewModel.initialLikesLoaded.filter { it }.first()
                // 찜 목록 로딩이 완료되었으므로, 이제 게스트하우스 목록을 불러옴
                loadMore(showOverlay = true) // ← 첫 로딩에는 오버레이 유지
            }
        }

        // 페이징 스크롤
        binding.guesthouseRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                if (dy <= 0) return
                val last = lm.findLastVisibleItemPosition()
                val total = lm.itemCount
                val threshold = 4
                if (!isLoading && !isLastPage && last >= total - threshold) {
                    // 페이지 추가 로드는 오버레이 없이(리스트 하단에서 자연스럽게)
                    loadMore(showOverlay = false)
                }
            }
        })

        // 찜 목록 변경 시 갱신
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.likedGuestHouseIds.collect {
                    if (adapter.itemCount > 0) {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun loadMore(showOverlay: Boolean) {
        if (isLoading || isLastPage) return
        isLoading = true
        if (showOverlay) showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // ViewModel이 서버 0-based로 보정해서 가져오고, 내부 캐시까지 갱신
                val next = viewModel.fetchPageAndCache(page, pageSize)

                if (page == 1) adapter.replaceAll(viewModel.items) else adapter.append(next)

                if (next.isEmpty()) {
                    isLastPage = true
                } else {
                    page += 1
                }

                // 현재 상태를 ViewModel에도 저장(뒤로가기 복원용)
                viewModel.nextPage = page
                viewModel.isLastPageCached = isLastPage

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                if (showOverlay) showLoading(false)
            }
        }
    }

    /* -------------------- 로딩 오버레이 & 도트 애니메이션 -------------------- */

    private fun showLoading(show: Boolean) {
        // include된 view_home_loading 의 root를 표시/숨김
        binding.homeLoading.root.isVisible = show
        if (show) startDotAnimation() else stopDotAnimation()
    }

    private fun startDotAnimation() {
        val dots = arrayOf(
            binding.homeLoading.dot1,
            binding.homeLoading.dot2,
            binding.homeLoading.dot3
        )
        dotJob?.cancel()
        dotJob = viewLifecycleOwner.lifecycleScope.launch {
            var i = 0
            while (isActive) {
                dots.forEachIndexed { idx, v ->
                    v.setImageResource(
                        if (idx == i) R.drawable.dot_green else R.drawable.dot_gray
                    )
                }
                i = (i + 1) % dots.size
                delay(300)
            }
        }
    }

    private fun stopDotAnimation() {
        dotJob?.cancel()
        dotJob = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopDotAnimation()
    }
}
