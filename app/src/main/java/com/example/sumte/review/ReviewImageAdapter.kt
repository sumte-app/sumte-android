package com.example.sumte.review

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.databinding.ItemReviewManageBinding

class ReviewImageAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ReviewImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemReviewManageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String) {
            Log.d("ImageAdapter", "Loading image with URL: $imageUrl")
            Glide.with(binding.root.context)
                .load(imageUrl)
                .into(binding.photoImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemReviewManageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        Log.d("ImageAdapter", "onCreateViewHolder called.")
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Log.d("ImageAdapter", "onBindViewHolder called for position: $position")
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int {
        val count = imageUrls.size
        Log.d("ImageAdapter", "getItemCount() called. Size is: $count")
        return count
    }
}