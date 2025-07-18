package com.example.sumte

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

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