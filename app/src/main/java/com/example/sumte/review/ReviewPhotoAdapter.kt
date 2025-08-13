package com.example.sumte.review

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.databinding.ItemReviewPhotoBinding

class ReviewPhotoAdapter(
    private val photoList: MutableList<Uri>,
    private val onDeleteClickListener: (Int) -> Unit
) : RecyclerView.Adapter<ReviewPhotoAdapter.PhotoViewHolder>() {

    // 뷰홀더
    inner class PhotoViewHolder(private val binding: ItemReviewPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            // Glide를 사용하여 Uri에서 이미지를 로드
            Glide.with(binding.root.context)
                .load(uri)
                .centerCrop()
                .into(binding.photoImageViewWrite)

            // 이미지 삭제 버튼 클릭 리스너
            binding.removeButton.setOnClickListener {
                onDeleteClickListener(adapterPosition)
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

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photoList[position])
    }

    override fun getItemCount(): Int = photoList.size
}
