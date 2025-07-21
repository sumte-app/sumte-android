package com.example.sumte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ItemGuesthouseBinding

class LikeAdapter(
    private val items: List<GuestHouse>,
    private val viewModel: GuestHouseViewModel
) : RecyclerView.Adapter<LikeAdapter.ViewHolder>() {

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
            guesthouseIv.setImageResource(guestHouse.imageResId)

            guesthouseHeartIv.setImageResource(
                if (viewModel.isLiked(guestHouse)) R.drawable.heart_home_filled else R.drawable.heart_home_empty
            )

            guesthouseHeartIv.setOnClickListener {
                viewModel.toggleLike(guestHouse)
                notifyItemRemoved(position)
            }
        }
    }
}