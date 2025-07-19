package com.example.a8thumcproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // EmailInputActivity로 이동
        val intent = Intent(this, EmailInputActivity::class.java)
        startActivity(intent)

        // 필요하면 MainActivity는 finish()로 종료해도 됨
        // finish()
    }
}