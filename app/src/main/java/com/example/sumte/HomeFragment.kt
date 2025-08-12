package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.mainLogoIv.setOnClickListener {
            startActivity(Intent(activity, ReviewWriteActivity::class.java))
        }
        binding.adsIv.setOnClickListener {
            startActivity(Intent(activity, ImageUploadActivity::class.java))
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[GuestHouseViewModel::class.java]


        adapter = GuestHouseAdapter(
            viewModel = viewModel,
            onItemClick = { guestHouse ->
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, HouseDetailFragment())
                    .addToBackStack(null)
                    .commit()
            }
        )

        val lm = LinearLayoutManager(requireContext())
        binding.guesthouseRv.layoutManager = lm
        binding.guesthouseRv.adapter = adapter

        // ★ 캐시가 있으면 즉시 복원(네트워크 X)
        if (viewModel.items.isNotEmpty()) {
            adapter.replaceAll(viewModel.items)
            page = viewModel.nextPage
            isLastPage = viewModel.isLastPageCached
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                // initialLikesLoaded가 true가 될 때까지 기다렸다가 한 번만 실행
                viewModel.initialLikesLoaded.filter { it }.first()
                // 찜 목록 로딩이 완료되었으므로, 이제 게스트하우스 목록을 불러옴
                loadMore()
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
                    loadMore()
                }
            }
        })

        binding.searchBoxLl.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java)
            intent.putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, "search")
            startActivity(intent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.likedGuestHouseIds.collect {
                    // 찜 목록이 로딩되거나 변경되었을 때, 어댑터에게 전체 데이터를 새로고침하라고 알려줌.
                    if (adapter.itemCount > 0) {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun loadMore() {
        if (isLoading || isLastPage) return
        isLoading = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // ★ ViewModel이 서버 0-based로 보정해서 가져오고, 내부 캐시까지 갱신
                val next = viewModel.fetchPageAndCache(page, pageSize)

                if (page == 1) adapter.replaceAll(viewModel.items) else adapter.append(next)

                if (next.isEmpty()) {
                    isLastPage = true
                } else {
                    page += 1
                }

                // ★ 현재 상태를 ViewModel에도 저장(뒤로가기 복원용)
                viewModel.nextPage = page
                viewModel.isLastPageCached = isLastPage

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }
}
