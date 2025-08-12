package com.example.sumte.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.R

class SearchActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        const val EXTRA_KEYWORD = "keyword"
        const val TYPE_SEARCH = "search"
        const val TYPE_SEARCH_RESULT = "search_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        if (savedInstanceState == null) {
            val fragmentType = intent.getStringExtra(EXTRA_FRAGMENT_TYPE) ?: TYPE_SEARCH
            val fragment = when (fragmentType) {
                TYPE_SEARCH_RESULT -> {
                    val keyword = intent.getStringExtra(EXTRA_KEYWORD) ?: ""
                    SearchResultFragment().apply {
                        arguments = Bundle().apply {
                            putString("keyword", keyword)
                        }
                    }
                }
                else -> SearchFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.search_container, fragment)
                .commit()
        }
    }
}