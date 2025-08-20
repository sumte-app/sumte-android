package com.example.sumte.review

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.R
import com.example.sumte.databinding.ItemReviewBinding


class ReviewManageAdapter(private val fragment : Fragment):RecyclerView.Adapter<ReviewManageAdapter.ReviewViewHolder>() {
    private val items = mutableListOf<MyReview>()

    fun addItems(newItems: List<MyReview>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }
    fun setItems(newList:List<MyReview>){
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, items.size)
    }

    // '실행 취소' 시 특정 위치에 아이템을 다시 추가하는 함수
    fun addItem(position: Int, item: MyReview) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    // '실행 취소'를 위해 삭제 전 아이템 정보를 가져오는 함수
    fun getItem(position: Int): MyReview? {
        return if (position >= 0 && position < items.size) {
            items[position]
        } else {
            null
        }
    }


    fun updateItem(position: Int, newReview: ReviewRequest2) {
        val old = items[position]
        items[position] = old.copy(
            reservationId = newReview.reservationId,
            contents = newReview.contents,
            score = newReview.score
        )
        notifyItemChanged(position)
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item: MyReview)=with(binding){
            itemReviewTitleTv.text=item.authorNickname
            itemReviewDateTv.text=item.createdAt.substring(0, 10)
            itemReviewSubtitleTv.text=item.roomName

            val starViews = listOf(
                itemReviewStar1,
                itemReviewStar2,
                itemReviewStar3,
                itemReviewStar4,
                itemReviewStar5
            )
            updateStars(starViews, item.score)

            itemReviewContentTv.text=item.contents

            // 중첩된 리사이클러뷰 설정
            if (!item.imageUrls.isNullOrEmpty()) {
                Log.d("ReviewAdapter", "Image list is not empty. Creating adapter.")
                reviewImageContainer.visibility = View.VISIBLE
                val imageAdapter = ReviewImageAdapter(item.imageUrls)

                reviewImageRv.apply {
                    layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = imageAdapter
                    visibility = View.VISIBLE
                    Log.d("ReviewAdapter", "ReviewImageAdapter and LayoutManager set successfully.")
                }
            } else {
                Log.d("ReviewAdapter", "Image list is empty or null. Hiding RecyclerView.")
                reviewImageRv.visibility = View.GONE
            }

            itemReviewEditTv.setOnClickListener {
                Log.d("ID_CHECK", "수정 버튼 클릭 - Intent에 담을 Review ID: ${item.id}")
                val intent = Intent(binding.root.context, ReviewWriteActivity::class.java).apply{
                    putExtra("isEditMode", true)
                    putExtra("reviewId", item.id)
                    putExtra("roomId", item.roomId)
                    putExtra("contents", item.contents)
                    putExtra("score", item.score)
                    putExtra("imageUrls", ArrayList(item.imageUrls ?: emptyList()))
                    putExtra("roomName", item.roomName)
                    putExtra("reservationId", item.reservationId)
                }
                binding.root.context.startActivity(intent)
            }

            itemReviewDeleteTv.setOnClickListener {
                ReviewDeleteAskDialog(
                    onConfirm = {
                        (fragment as? ReviewManage)
                            ?.deleteReview(item.id, adapterPosition)
                    }
                ).show(fragment.parentFragmentManager, "review_delete_ask")
            }
        }

        // 점수만큼 star_fill, 나머지는 star_empty로 교체
        private fun updateStars(stars:List<ImageView>, score:Int){
            stars.forEachIndexed{ index, iv ->
                iv.setImageResource(
                    if(index<score) R.drawable.star_fill
                    else R.drawable.star_empty
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewViewHolder {
        val binding=ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int=items.size
}