package com.example.sumte.housedetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ItemRoomDetailBinding

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
                onReserveClick(room)
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