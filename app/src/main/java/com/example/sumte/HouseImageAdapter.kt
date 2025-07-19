package com.example.sumte

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ItemHouseImageBinding

class HouseImageAdapter(
    private val images: List<Int>
) : RecyclerView.Adapter<HouseImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemHouseImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemHouseImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.binding.ivPageImage.setImageResource(images[position])
    }

    override fun getItemCount(): Int = images.size
}
