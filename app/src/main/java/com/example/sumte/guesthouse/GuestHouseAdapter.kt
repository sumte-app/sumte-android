package com.example.sumte.guesthouse

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.R
import com.example.sumte.databinding.ItemGuesthouseBinding

class GuestHouseAdapter(
    private val viewModel: GuestHouseViewModel,
    private val onItemClick: (GuestHouse) -> Unit
) : RecyclerView.Adapter<GuestHouseAdapter.ViewHolder>() {

    private var items: List<GuestHouse> = emptyList()
    private var likedIds: Set<Int> = emptySet()

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
            guesthouseTitleTv.text = guestHouse.title
            guesthouseLocationTv.text = guestHouse.location
            guesthousePriceTv.text = guestHouse.price

            // ★ 새로 추가: 평점/리뷰/체크인
            guesthouseRatingTv.text = guestHouse.averageScore
                ?.let { String.format("%.1f", it) } ?: "-"
            guesthouseReviewCountTv.text = "${guestHouse.reviewCount ?: 0}"
            guesthouseTimeTv.text = guestHouse.time.let {
                // time이 이미 "HH:mm"으로 매핑돼 있다면 그대로 사용, 아니면 유틸로 정리
                it.ifBlank { "-" } // 또는 it.toHhMm()
            }

            val url = guestHouse.imageUrl?.trim()
            val ph = guestHouse.imageResId
            Log.d("IMG", "bind ghId=${guestHouse.id} url=$url")

            Glide.with(root).clear(guesthouseIv)
            guesthouseIv.setImageResource(ph)

            if (!url.isNullOrEmpty()) {
                Glide.with(root)
                    .load(url)
                    .placeholder(ph)
                    .error(ph)
                    .centerCrop()
                    .into(guesthouseIv)
            }

            val isLiked = viewModel.isLiked(guestHouse)
            Log.d("Adapter_Check",
                "게스트하우스 ID: ${guestHouse.id} | isLiked=$isLiked")

            guesthouseHeartIv.setImageResource(
                if (isLiked) R.drawable.heart_home_filled else R.drawable.heart_home_empty
            )

            root.setOnClickListener { onItemClick(guestHouse) }
            guesthouseHeartIv.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                viewModel.toggleLike(guestHouse) { notifyItemChanged(pos) }
            }
        }
    }

    fun updateLikes(newLikedIds: Set<Int>) {
        this.likedIds = newLikedIds
        notifyDataSetChanged()
    }

    fun replaceAll(newItems: List<GuestHouse>) {
        items = newItems.toList()
        notifyDataSetChanged()
    }

    fun append(newItems: List<GuestHouse>) {
        if (newItems.isEmpty()) return
        val start = items.size
        items = items + newItems
        notifyItemRangeInserted(start, newItems.size)
    }

    fun updateItems(newItems: List<GuestHouse>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun submit(newItems: List<GuestHouse>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
