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

            val url = guestHouse.imageUrl?.trim()
            val ph = guestHouse.imageResId
            Log.d("IMG", "bind ghId=${guestHouse.id} url=$url")

            // 재활용 대비: 이전 이미지 요청/표시 초기화
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
            Log.d(
                "Adapter_Check",
                "게스트하우스 ID: ${guestHouse.id} (타입: ${guestHouse.id::class.simpleName}) | isLiked 결과: $isLiked"
            )

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

}
