package com.example.sumte.housedetail

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
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

    inner class RoomViewHolder(private val binding: ItemRoomDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(room: RoomInfo) = with(binding) {
            tvRoomName.text = room.name
            tvRoomPrice.text = "${String.format("%,d", room.price)}원"
            tvRoomCapacity.text = "기준인원 ${room.standardCount}인 (정원 ${room.totalCount}인)"
            tvCheckInOut.text = "체크인 ${room.checkin} · 체크아웃 ${room.checkout}"

            Glide.with(ivRoomImage.context)
                .load(room.imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .into(ivRoomImage)

            ivReserve.setOnClickListener {
                //root.context.startActivity(Intent(root.context, PaymentActivity::class.java))
                onReserveClick(room)
            }
            //상세보기
            tvRoomDetail.setOnClickListener {
                Log.d("RoomInfoAdapter", "Clicked room.id = ${room.id}") // 확인용
                val intent = Intent(root.context, ActivityRoomDetail::class.java)
                intent.putExtra("roomId", room.id) // ★ 여기서 roomId 전달
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

    /** ViewModel에서 받은 리스트로 갱신 */
    fun submitList(newList: List<RoomInfo>) {
        roomList = newList
        notifyDataSetChanged()
    }
}