package com.example.sumte.review

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ItemReviewBinding

class ReviewAdapter(private val reviewList:List<Review>) :RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>(){

    inner class ReviewViewHolder(val binding: ItemReviewBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(review: Review){
            binding.itemReviewTitleTv.text=review.title
            binding.itemReviewContentTv.text=review.content
            binding.itemReviewDateTv.text=review.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun getItemCount()=reviewList.size

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }

}