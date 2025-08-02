package com.example.sumte.housedetail

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.housedetail.ActivityRoomDetail
import com.example.sumte.databinding.ItemRoomDetailBinding
import com.example.sumte.payment.PaymentActivity

class RoomInfoAdapter(
    private val roomList: List<RoomInfo>,
    private val onReserveClick: (RoomInfo) -> Unit
) : RecyclerView.Adapter<RoomInfoAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(private val binding: ItemRoomDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(room: RoomInfo) {
            binding.tvRoomName.text = room.name
            binding.tvRoomPrice.text = "${String.format("%,d", room.price)}원"
            binding.tvRoomCapacity.text = "기준인원 ${room.person}인 (정원 ${room.maxPerson}인)"
            binding.tvCheckInOut.text = "체크인 ${room.checkInTime} · 체크아웃 ${room.checkOutTime}"
            binding.ivRoomImage.setImageResource(room.imageResId)

            binding.ivReserve.setOnClickListener {
                val intent = Intent(binding.root.context, PaymentActivity::class.java)
                binding.root.context.startActivity(intent)
                onReserveClick(room)
            }

            binding.tvRoomDetail.setOnClickListener {
                val intent = Intent(binding.root.context, ActivityRoomDetail::class.java)
                binding.root.context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(roomList[position])


    }

    override fun getItemCount(): Int = roomList.size
}