package com.example.sumte.review

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.MyReview
import com.example.sumte.R
import com.example.sumte.databinding.ItemReviewBinding

class ReviewManageAdapter(private val fragment : Fragment):RecyclerView.Adapter<ReviewManageAdapter.ReviewViewHolder>() {
    private val items = mutableListOf<MyReview>()

    // 외부에서 새 리스트 넣어줄 때 호출
    fun submitList(list:List<MyReview>){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
    fun setItems(newList:List<MyReview>){
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item: MyReview)=with(binding){
            itemReviewTitleTv.text=item.authorNickname
            itemReviewDateTv.text=item.createdAt.substring(0, 10)

            val starViews = listOf(
                itemReviewStar1,
                itemReviewStar2,
                itemReviewStar3,
                itemReviewStar4,
                itemReviewStar5
            )
            updateStars(starViews, item.score)

            itemReviewContentTv.text=item.contents

            // 사진 부분
//            itemReviewContentTv.text=item.contents
//            Glide.with(root).load(item.imageUrl)
//                .placeholder(R.drawable.like_house2)
//                .error(R.drawable.like_house2)
//                .into(photoImageView)

            itemReviewEditIv.setOnClickListener {

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