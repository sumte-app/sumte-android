package com.example.sumte.mybook

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.R
import com.example.sumte.databinding.ItemBooklistBinding
import com.example.sumte.databinding.ItemHistoryBinding
import com.example.sumte.review.ReviewWriteActivity
import com.example.sumte.search.HistoryAdapter
import com.example.sumte.search.HistoryAdapter.HistoryViewHolder

//이미지 일단 제외
class BookedAdapter(
    private val items: List<BookedData>,
    private val fragment: Fragment  // Fragment 넘기기
) : RecyclerView.Adapter<BookedAdapter.BookedViewHolder>() {

    inner class BookedViewHolder(private var binding : ItemBooklistBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun bind(bookedData: BookedData) {
            binding.houseName.text = bookedData.houseName
            binding.bookedDate.text = bookedData.bookedDate
            binding.roomType.text = bookedData.roomType
            binding.startDate.text = bookedData.startDate
            binding.endDate.text = bookedData.endDate
            binding.dateCount.text = bookedData.dateCount
            binding.adultCount.text = "${bookedData.adultCount}명"

            // childCount가 0이면 GONE, 아니면 보여주기
            if (bookedData.childCount == 0) {
                binding.childCount.visibility = View.GONE
                binding.countComma.visibility = View.GONE  // 쉼표 TextView가 있다면
            } else {
                binding.childCount.visibility = View.VISIBLE
                binding.childCount.text = "${bookedData.childCount}명"
                binding.countComma.visibility = View.VISIBLE
            }

            //리뷰 작성가능시에만 후기작성
            //binding.reviewBtn.visibility = if (bookedData.canWriteReview) View.VISIBLE else View.GONE



            binding.reviewBtn.setOnClickListener {
                // 리뷰작성 페이지 이동
            }

            //상세보기 페이지
            binding.detailBox.setOnClickListener {
                val detailFragment = BookedDetailFragment()
                val bundle = Bundle().apply {
                    putParcelable("bookedData", bookedData)
                }
                detailFragment.arguments = bundle

                fragment.parentFragmentManager.beginTransaction()
                    .replace(R.id.booked_list_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookedViewHolder {
        val binding = ItemBooklistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookedViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


}