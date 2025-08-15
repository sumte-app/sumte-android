package com.example.sumte.review

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ItemReviewListBinding

class ReviewListAdapter(private var reviews: List<ReviewItem>) :
    RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder>() {

    // ViewHolder: 각 아이템의 뷰를 보관하는 객체
    inner class ReviewViewHolder(private val binding: ItemReviewListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: ReviewItem) {
            // ⚠️ item_review_list.xml에 정의된 View ID에 맞게 수정해야 합니다.
            // 예시: binding.nicknameTextView.text = review.authorNickname
            // 예시: binding.contentsTextView.text = review.contents
            // 예시: binding.scoreRatingBar.rating = review.score.toFloat()
            // 예시: binding.roomNameTextView.text = review.roomName
            // ... 나머지 데이터 바인딩 ...
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding =
            ItemReviewListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    // 외부에서 데이터를 업데이트하기 위한 함수
    fun updateData(newReviews: List<ReviewItem>) {
        reviews = newReviews
        notifyDataSetChanged() // 데이터가 변경되었음을 어댑터에 알림
    }
}