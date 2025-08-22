package com.example.sumte.housedetail

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.housedetail.ActivityRoomDetail
import com.example.sumte.databinding.ItemRoomDetailBinding
import com.example.sumte.payment.PaymentActivity


class RoomInfoAdapter(
    private var roomList: List<RoomInfo>,
    private val onReserveClick: (RoomInfo) -> Unit
) : RecyclerView.Adapter<RoomInfoAdapter.RoomViewHolder>() {

    private var peopleCount: Int = 0

    inner class RoomViewHolder(private val binding: ItemRoomDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(room: RoomInfo) = with(binding) {
            tvRoomName.text = room.name
            tvRoomPrice.text = "${String.format("%,d", room.price)}원"
            tvRoomCapacity.text = "기준인원 ${room.standardCount}인"
            tvRoomTotalCapacity.text = "(정원 ${room.totalCount}인)"
            tvCheckInOut.text = "체크인 ${room.checkin} · 체크아웃 ${room.checkout}"


            val enabled = (room.reservable == true) && (peopleCount <= room.totalCount)
            ivReserve.isEnabled = enabled
            ivReserve.isClickable = enabled
            ivReserve.text = if (enabled) "예약하기" else "예약불가"


            Glide.with(ivRoomImage.context)
                .load(room.imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .into(ivRoomImage)

            Log.d("RoomInfoAdapter", "roomId=${room.id}, reservable=${room.reservable}" )
            ivReserve.setOnClickListener(null)
            ivReserve.setOnClickListener {
                if (!enabled) {
                    val msg = if (room.reservable != true) {
                        "해당 객실은 현재 예약 마감 상태입니다."
                    } else {
                        "선택 인원이 정원을 초과했습니다."
                    }
                    Toast.makeText(root.context, msg, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                onReserveClick(room)
            }

            //상세보기
            tvRoomDetail.setOnClickListener {
                Log.d("RoomInfoAdapter", "Clicked room.id = ${room.id}")
                val intent = Intent(root.context, ActivityRoomDetail::class.java)
                intent.putExtra("roomId", room.id)
                intent.putExtra("reservableBase", room.reservable == true)
                intent.putExtra("totalCountBase", room.totalCount)
                root.context.startActivity(intent)
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

    fun submitList(newList: List<RoomInfo>) {
        roomList = newList
        notifyDataSetChanged()
    }

    fun updatePeopleCount(count: Int) {
        peopleCount = count
        notifyDataSetChanged() // 필요 시 payload 최적화 가능
    }
}