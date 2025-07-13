package com.example.sumte

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.databinding.ItemReviewPhotoBinding

class ReviewPhotoAdapter(
    private val photos: MutableList<Uri>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<ReviewPhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(private val binding: ItemReviewPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) = with(binding) {
            Glide.with(root)
                .load(uri)
                .centerCrop()
                .into(photoImageView)

            removeButton.setOnClickListener {
                onRemoveClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemReviewPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }
}
