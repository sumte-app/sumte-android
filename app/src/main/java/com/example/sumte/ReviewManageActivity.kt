package com.example.sumte

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ReviewManageActivity :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_manage)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.review_manage_container, ReviewManage())
                .commit()
        }

    }
}