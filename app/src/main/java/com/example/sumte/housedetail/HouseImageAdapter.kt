package com.example.sumte.housedetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.R
import com.example.sumte.databinding.ItemHouseImageBinding

class HouseImageAdapter(
    private val onClick: (String) -> Unit = {}
) : ListAdapter<String, HouseImageAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHouseImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val url = getItem(position)
        with(holder.binding.ivPageImage) {
            // 필요하면 사이즈 힌트: .override(width, height)
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.sample_house1)
                .error(R.drawable.sample_house1)
                .into(this)
        }
        holder.binding.root.setOnClickListener { onClick(url) }
    }

    override fun onViewRecycled(holder: VH) {
        // 재활용 시 이미지 정리(깜박임/누수 예방)
        Glide.with(holder.binding.ivPageImage).clear(holder.binding.ivPageImage)
        super.onViewRecycled(holder)
    }

    class VH(val binding: ItemHouseImageBinding) : RecyclerView.ViewHolder(binding.root)
}
