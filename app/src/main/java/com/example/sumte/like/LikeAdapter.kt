package com.example.sumte.like

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.GuesthouseSummaryDto
import com.example.sumte.R
import com.example.sumte.databinding.ItemGuesthouseBinding

class LikeAdapter(
    private val items: MutableList<GuesthouseSummaryDto>,
    private val onLikeRemovedListener: OnLikeRemovedListener
) : RecyclerView.Adapter<LikeAdapter.ViewHolder>() {

    interface OnLikeRemovedListener {
        fun onLikeRemoved(guestHouse: GuesthouseSummaryDto)
    }

    inner class ViewHolder(val binding: ItemGuesthouseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGuesthouseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guestHouse = items[position]
        with(holder.binding) {
            val ghId= guestHouse.id
            guesthouseTitleTv.text = guestHouse.name ?: "이름 정보 없음"
            guesthouseLocationTv.text = guestHouse.addressRegion ?: "위치 정보 없음"
            guesthousePriceTv.text = if (guestHouse.minPrice != null && guestHouse.minPrice > 0) {
                String.format("%,d원", guestHouse.minPrice)
            } else {
                "가격 정보 없음"
            }

            guesthouseTimeTv.text = if (!guestHouse.checkInTime.isNullOrBlank()) {
                "입실 ${guestHouse.checkInTime}"
            } else {
                "시간 정보 없음"
            }

            // 평점과 리뷰 개수를 함께 표시 (예: ⭐ 4.5 (65))
            val score = guestHouse.averageScore
            val reviews = guestHouse.reviewCount
            if (score != null && score > 0 && reviews != null) {
                guesthouseRatingTv.text = "⭐ %.1f (%d)".format(score, reviews)
            } else {
                guesthouseRatingTv.text = "평점 정보 없음"
            }

            if (!guestHouse.imageUrl.isNullOrBlank()) {
                Glide.with(root.context)
                    .load(guestHouse.imageUrl)
                    .placeholder(R.drawable.sample_house3)
                    .error(R.drawable.sample_house3)
                    .into(guesthouseIv)
            } else {
                guesthouseIv.setImageResource(R.drawable.sample_house2)
            }

            guesthouseHeartIv.setImageResource(R.drawable.heart_home_filled)

            guesthouseHeartIv.setOnClickListener {
                Log.d("LikeAdapter_Check", "게스트하우스 ID: $ghId")
                onLikeRemovedListener.onLikeRemoved(guestHouse)
            }
        }
    }

    fun setItems(newItems: List<GuesthouseSummaryDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeItem(guestHouse: GuesthouseSummaryDto) {
        val index = items.indexOfFirst { it.id == guestHouse.id }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}