package com.example.sumte.review
import kotlin.collections.get



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.R

import com.example.sumte.databinding.ItemReviewCardBinding


class ReviewCardAdapter(private val reviews: List<Review>) :
    RecyclerView.Adapter<ReviewCardAdapter.ReviewCardViewHolder>() {

    inner class ReviewCardViewHolder(val binding: ItemReviewCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReviewCardBinding.inflate(inflater, parent, false)
        return ReviewCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewCardViewHolder, position: Int) {
        val review = reviews[position]
        with(holder.binding) {
            tvDate.text = review.date
            tvReview.text = review.content
            rbStar.rating = review.rating

            if (!review.imageUrls.isNullOrEmpty()) {
                Glide.with(ivImage.context)
                    .load(review.imageUrls[0])
                    .centerCrop()
                    .into(ivImage)
            } else {
                ivImage.setImageResource(R.drawable.sample_room1) // 대체 이미지
            }
        }
    }

    override fun getItemCount(): Int = reviews.size
}
//
//class ReviewCardAdapter(private val reviews: List<Review>) :
//    RecyclerView.Adapter<ReviewCardAdapter.ReviewCardViewHolder>() {
//
//    inner class ReviewCardViewHolder(val binding: ItemReviewCardBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewCardViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding = ItemReviewCardBinding.inflate(inflater, parent, false)
//        return ReviewCardAdapter.ReviewCardViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ReviewCardViewHolder, position: Int) {
//        val review = reviews[position]
//        with(holder.binding) {
//            tvDate.text = review.date
//            tvReview.text = review.content
//            rbStar.rating = review.rating
//
//            if (!review.imageUrls.isNullOrEmpty()) {
//                Glide.with(ivImage.context)
//                    .load(review.imageUrls[0])
//                    .centerCrop()
//                    .into(ivImage)
//            } else {
//                ivImage.setImageResource(R.drawable.sample_room1) // 대체 이미지
//            }
//        }
//    }
//
//    override fun getItemCount(): Int = reviews.size
//}