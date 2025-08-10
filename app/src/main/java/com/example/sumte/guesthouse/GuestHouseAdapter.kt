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

    // 불변 리스트 유지
    var items: List<GuestHouse> = emptyList()
        private set

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
            // URL 있으면 Glide, 없으면 로컬 리소스
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
                viewModel.toggleLike(guestHouse)
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos)
            }
        }
    }


    fun replaceAll(newItems: List<GuestHouse>) {
        items = newItems.toList()
        notifyDataSetChanged()
    }

    // 다음 페이지 “붙이기” (copy-on-write)
    fun append(newItems: List<GuestHouse>) {
        if (newItems.isEmpty()) return
        val start = items.size
        items = items + newItems
        notifyItemRangeInserted(start, newItems.size)
    }

}
