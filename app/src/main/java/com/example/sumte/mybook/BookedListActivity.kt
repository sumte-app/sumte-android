package com.example.sumte.mybook

import BookedListMainFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.R

class BookedListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booked_list)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.booked_list_container, BookedListMainFragment())
                .commit()
        }

    }
}