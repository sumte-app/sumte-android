package com.example.sumte

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.review.ReviewManage

class MyReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_review)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.my_review_container, ReviewManage())
                .commit()
        }

    }
}