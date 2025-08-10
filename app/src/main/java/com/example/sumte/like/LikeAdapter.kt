package com.example.sumte.like

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.R
import com.example.sumte.databinding.ItemGuesthouseBinding

class LikeAdapter(
    private val items: MutableList<GuestHouseResponse>,
    private val onLikeRemovedListener: OnLikeRemovedListener
) : RecyclerView.Adapter<LikeAdapter.ViewHolder>() {

    interface OnLikeRemovedListener {
        fun onLikeRemoved(guestHouse: GuestHouseResponse)
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
            guesthouseTitleTv.text = guestHouse.name
            guesthouseLocationTv.text = guestHouse.addressRegion
            guesthousePriceTv.text = guestHouse.minPrice.toString()
            Glide.with(root.context)
                .load(guestHouse.imageUrls)
                .into(guesthouseIv)

            guesthouseHeartIv.setImageResource(R.drawable.heart_home_filled)

            guesthouseHeartIv.setOnClickListener {
                onLikeRemovedListener.onLikeRemoved(guestHouse)
            }
        }
    }

    fun setItems(newItems: List<GuestHouseResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeItem(guestHouse: GuestHouseResponse) {
        val index = items.indexOfFirst { it.id == guestHouse.id }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun addItem(guestHouse: GuestHouseResponse) {
        items.add(0, guestHouse)
        notifyItemInserted(0)
    }
}