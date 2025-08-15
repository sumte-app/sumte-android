package com.example.sumte.search

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.R

class BookInfoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        const val EXTRA_KEYWORD = "keyword"
        const val EXTRA_SOURCE = "source" //소스

        const val TYPE_DATE = "date"
        const val TYPE_COUNT = "count"
        const val TYPE_SEARCH_RESULT = "search_result"
    }

    private var source: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_info)

        source = intent.getStringExtra(EXTRA_SOURCE)

        if (savedInstanceState == null) {
            val fragmentType = intent.getStringExtra(EXTRA_FRAGMENT_TYPE) ?: TYPE_DATE
            val fragment = when (fragmentType) {
                TYPE_COUNT -> BookInfoCountFragment()
                TYPE_SEARCH_RESULT -> {
                    val keyword = intent.getStringExtra(EXTRA_KEYWORD) ?: ""
                    SearchResultFragment().apply {
                        arguments = Bundle().apply {
                            putString("keyword", keyword)
                        }
                    }
                }
                "search" -> SearchFragment()
                else -> BookInfoDateFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .commit()
        }
    }

    fun onApplyClicked() {
        when (source) {
            "house_detail" -> {
                finish() // HouseDetail에서 왔으면 단순 종료
            }
            else -> {
                val keyword = "" //키워드
                val fragment = SearchResultFragment().apply {
                    arguments = Bundle().apply {
                        putString("keyword", keyword)
                    }
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.book_info_container, fragment)
                    .addToBackStack(null) // 뒤로가기 시 이전 화면으로
                    .commit()
            }
        }
    }
}
