package com.example.sumte

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class BookInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_info)

        // 처음에는 BookInfoFragment로 시작
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, BookInfoDateFragment())
                .commit()
        }

    }
}