package com.example.sumte.review

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.R
import com.example.sumte.databinding.ItemReviewListBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReviewListAdapter(private var reviews: List<ReviewItem>) :
    RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder>() {

    // ViewHolder: 각 아이템의 뷰를 보관하는 객체
    inner class ReviewViewHolder(private val binding: ItemReviewListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: ReviewItem)=with(binding){
            binding.itemReviewListNicknameTv.text=review.authorNickname
            binding.itemReviewSubtitleTv.text=review.roomName
            binding.itemReviewContentTv.text=review.contents

            // 서버에서 받은 원본 문자열을 LocalDateTime 객체로 파싱
            val parsedDateTime = LocalDateTime.parse(review.createdAt)

            // 원하는 날짜 형식(yyyy-MM-dd)으로 포맷터 제작.
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            // 3. 파싱된 객체를 원하는 형식의 문자열로 변환
            val formattedDate = parsedDateTime.format(formatter)
            binding.itemReviewDateTv.text=formattedDate

            val starViews = listOf(
                itemReviewListStar1,
                itemReviewListStar2,
                itemReviewListStar3,
                itemReviewListStar4,
                itemReviewListStar5
            )
            updateStars(starViews, review.score)

            // 중첩된 리사이클러뷰 설정
            if (!review.imageUrls.isNullOrEmpty()) {
                Log.d("ReviewListAdapter", "Image list is not empty. Creating adapter.")
                reviewListImageContainer.visibility = View.VISIBLE
                val imageAdapter = ReviewImageAdapter(review.imageUrls)

                reviewListImageRv.apply {
                    layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = imageAdapter
                    visibility = View.VISIBLE
                    Log.d("ReviewListAdapter", "ReviewImageAdapter and LayoutManager set successfully.")
                }
            } else {
                Log.d("ReviewListAdapter", "Image list is empty or null. Hiding RecyclerView.")
                reviewListImageRv.visibility = View.GONE
            }
        }



        private fun updateStars(stars:List<ImageView>, score:Int){
            stars.forEachIndexed{ index, iv ->
                iv.setImageResource(
                    if(index<score) R.drawable.star_fill
                    else R.drawable.star_empty
                )
            }
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