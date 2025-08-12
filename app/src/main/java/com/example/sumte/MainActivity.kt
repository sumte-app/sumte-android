package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.databinding.ActivityMainBinding
import com.example.sumte.housedetail.HouseDetailFragment
import com.example.sumte.like.LikeFragment
import com.example.sumte.search.BookInfoActivity
import com.example.sumte.search.SearchFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var lastSelectedItemId: Int = R.id.fragment_home
    private lateinit var bookInfoLauncher: ActivityResultLauncher<Intent>
    private var previousTabId: Int = R.id.fragment_home  // 기본값


    private fun initBottomNavigation() {
        // 초기 프래그먼트
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, HomeFragment())
            .commitAllowingStateLoss()

        binding.bottomNavView.setOnItemSelectedListener { item ->
            lastSelectedItemId = item.itemId // ✅ 이걸 항상 저장

            when (item.itemId) {
                R.id.fragment_home -> {
                    previousTabId = R.id.fragment_home
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment())
                        .commitAllowingStateLoss()
                    true
                }
                R.id.fragment_search -> {
                    val intent = Intent(this, BookInfoActivity::class.java)
                    intent.putExtra(BookInfoActivity.EXTRA_FRAGMENT_TYPE, "search") // ✅ 추가
                    bookInfoLauncher.launch(intent)
                    false // 선택 상태 유지 X
                }
                R.id.fragment_favorite -> {
                    previousTabId = R.id.fragment_favorite
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, LikeFragment())
                        .commitAllowingStateLoss()
                    true
                }
                R.id.fragment_my -> {
                    previousTabId = R.id.fragment_my
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MyFragment())
                        .commitAllowingStateLoss()
                    true
                }
                else -> false
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        // 항상 호출되어야 함
        initBottomNavigation()

        // Search에서 BookInfo 갔다가 돌아왔을 때 처리
        bookInfoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    binding.bottomNavView.selectedItemId = previousTabId
                }
            }

        // 프래그먼트 변경 감지 → 바텀네비 상태 복원
        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.findFragmentById(R.id.main_container)

            binding.bottomNavView.visibility =
                if (current is SearchFragment || current is HouseDetailFragment) View.GONE else View.VISIBLE

            val targetId = when (current) {
                is HomeFragment -> R.id.fragment_home
                is LikeFragment -> R.id.fragment_favorite
                is MyFragment -> R.id.fragment_my
                else -> return@addOnBackStackChangedListener
            }
            binding.bottomNavView.menu.findItem(targetId).isChecked = true
        }
    }

}