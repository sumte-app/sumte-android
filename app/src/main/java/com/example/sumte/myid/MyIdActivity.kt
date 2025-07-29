package com.example.sumte.myid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.R

class MyIdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_id)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.my_id_container, MyIdMainFragment())
                .commit()
        }

    }
}