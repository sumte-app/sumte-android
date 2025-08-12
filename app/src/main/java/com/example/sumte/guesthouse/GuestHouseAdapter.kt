package com.example.sumte.guesthouse

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

            val ph = guestHouse.imageResId
            if (!guestHouse.imageUrl.isNullOrBlank()) {
                Glide.with(root).load(guestHouse.imageUrl)
                    .placeholder(ph).error(ph).into(guesthouseIv)
            } else {
                guesthouseIv.setImageResource(ph)
            }

            val isLiked = viewModel.isLiked(guestHouse)
            guesthouseHeartIv.setImageResource(
                if (isLiked) R.drawable.heart_home_filled else R.drawable.heart_home_empty
            )

            root.setOnClickListener { onItemClick(guestHouse) }

            guesthouseHeartIv.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                viewModel.toggleLike(guestHouse) {
                    // ViewModel의 작업이 끝난 후 UI를 갱신합니다.
                    notifyItemChanged(pos)
                }
            }
        }
    }

    fun updateLikes(newLikedIds: Set<Int>) {
        this.likedIds = newLikedIds
        notifyDataSetChanged() // 찜 목록이 바뀌었으니 화면 전체를 새로고침
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
}
