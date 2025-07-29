package com.example.sumte.roomregister

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.RetrofitClient
import com.example.sumte.databinding.ActivityRoomRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomRegisterBinding
    private val TAG = "RoomRegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val guesthouseId = intent.getIntExtra("guesthouseId", -1)

        binding.btnSubmit.setOnClickListener {
            val room = RoomRegisterRequest(
                name = binding.etRoomName.text.toString(),
                content = binding.etRoomContent.text.toString(),
                price = binding.etRoomPrice.text.toString().toIntOrNull() ?: 0,
                checkin = Time(15, 0, 0, 0),
                checkout = Time(11, 0, 0, 0),
                standartCount = binding.etStandardCount.text.toString().toIntOrNull() ?: 0,
                totalCount = binding.etTotalCount.text.toString().toIntOrNull() ?: 0,
                imageUrl = binding.etImageUrl.text.toString()
            )
            Log.d(TAG, "Sending room register request: $room")

            RetrofitClient.roomService.registerRoom(guesthouseId, room)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RoomRegisterActivity, "객실 등록 성공!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@RoomRegisterActivity, "등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "등록 실패 - code=${response.code()}, errorBody=${response.errorBody()?.string()}")
                            finish()
                        }

                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@RoomRegisterActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}