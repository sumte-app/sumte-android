package com.example.sumte.housedetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.R
import com.example.sumte.databinding.ItemRoomImageBinding

class RoomImagePagerAdapter(
    private var items: List<String> = emptyList()
) : RecyclerView.Adapter<RoomImagePagerAdapter.VH>() {

    inner class VH(val binding: ItemRoomImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRoomImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        Glide.with(holder.binding.img)
            .load(items[position])
            .placeholder(R.color.gray400)
            .into(holder.binding.img)
    }

    fun submit(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}