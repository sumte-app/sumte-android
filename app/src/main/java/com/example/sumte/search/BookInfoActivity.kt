package com.example.sumte.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.R

class BookInfoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        const val EXTRA_KEYWORD = "keyword"

        const val TYPE_DATE = "date"
        const val TYPE_COUNT = "count"
        const val TYPE_SEARCH_RESULT = "search_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_info)

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
                "search" -> SearchFragment() // ★ 여기 추가
                else -> BookInfoDateFragment()
            }


            supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .commit()
        }
    }
}
