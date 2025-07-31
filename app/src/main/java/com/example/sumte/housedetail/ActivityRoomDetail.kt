package com.example.sumte.housedetail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.databinding.ActivityRoomDetailBinding

class ActivityRoomDetail : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 넘어온 데이터 받기
        val roomId = intent.getIntExtra("roomId", -1)


    }
}