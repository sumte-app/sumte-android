package com.example.sumte.mybook

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.ApiClient
import com.example.sumte.R
import com.example.sumte.databinding.ItemBooklistBinding
import com.example.sumte.databinding.ItemHistoryBinding
import com.example.sumte.review.ReviewBookedWriteActivity
import com.example.sumte.review.ReviewRequest
import com.example.sumte.review.ReviewWriteActivity
import com.example.sumte.search.HistoryAdapter
import com.example.sumte.search.HistoryAdapter.HistoryViewHolder
import kotlinx.coroutines.launch


class BookedAdapter(
    private var items: List<BookedData>,
    private val fragment: Fragment,
) : RecyclerView.Adapter<BookedAdapter.BookedViewHolder>() {
    //ui전달부분
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
            if (bookedData.childCount == 0) {
                binding.childCount.visibility = View.GONE
                binding.countComma.visibility = View.GONE
            } else {
                binding.childCount.visibility = View.VISIBLE
                binding.childCount.text = "${bookedData.childCount}명"
                binding.countComma.visibility = View.VISIBLE
            }
            // 후기 작성 여부에 따른 버튼 변경
            if(bookedData.reviewWritten){
                binding.reviewWriteBtn.visibility=View.GONE
                binding.reviewWrittenBtn.visibility=View.VISIBLE
            }else{
                binding.reviewWriteBtn.visibility=View.VISIBLE
                binding.reviewWrittenBtn.visibility=View.GONE
            }

            binding.reviewWriteBtn.setOnClickListener {
                fragment.lifecycleScope.launch {
                    try {
                        // 서버에 보낼 "빈 리뷰" 데이터 생성
                        val requestBody = ReviewRequest(
                            roomId = bookedData.roomId,
                            contents = "",
                            score = 1
                        )
                        Log.d("ReviewAPI_Debug", "[리팩토링 후] Request Body: $requestBody")

                        // 리뷰 등록 API 호출
                        val response = ApiClient.reviewService.postReview(requestBody)

                        if (response.isSuccessful) {
                            // API 호출 성공 시, 응답으로 받은 reviewId 추출
                            val reviewId = response.body()
                            if (reviewId != null) {
                                // 받아온 reviewId와 함께 ReviewWriteActivity 시작
                                val intent = Intent(itemView.context, ReviewBookedWriteActivity::class.java)
                                intent.putExtra("BookedRoomId", bookedData.roomId)
                                intent.putExtra("isReviewMode", true)
                                intent.putExtra("BookedReviewId", reviewId)
                                itemView.context.startActivity(intent)
                            } else {
                                Toast.makeText(itemView.context, "리뷰 ID를 받지 못했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // API 호출 실패
                            val errorBody = response.errorBody()?.string() ?: "No error body"
                            Log.e("ReviewAPI_Debug", "[리팩토링 후] API Error - Code: ${response.code()}, Body: $errorBody")
                            Toast.makeText(itemView.context, "리뷰 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("BookedAdapter", "Failed to post review: ${response.code()}")
                        }


                        //취소시
                        if (bookedData.status == "CANCELED") {
                            val dimAlpha = 0.5f
                            binding.detailImg.alpha = dimAlpha
                            binding.houseName.alpha = dimAlpha
                            binding.roomType.alpha = dimAlpha
                            binding.selectedDate.alpha = dimAlpha
                            binding.selectedCount.alpha = dimAlpha

                            binding.status.text = "취소완료"
                        }

                        //리뷰 작성가능시에만 후기작성
                        binding.reviewWriteBtn.visibility = if (bookedData.canWriteReview) View.VISIBLE else View.GONE


                        binding.reviewWriteBtn.setOnClickListener {
                            // 리뷰작성 페이지 이동
                        }
                    } catch (e: Exception) {
                        // 네트워크 오류 등 예외 발생
                        Log.e("ReviewAPI_Debug", "[리팩토링 후] Exception in postReview", e)
                        Log.e("BookedAdapter", "Exception in postReview", e)
                    }
                }
            }

            //상세보기 페이지
            binding.detailBox.setOnClickListener {
                val detailFragment = BookedDetailFragment()
                val bundle = Bundle().apply {
                    putInt("reservationId", bookedData.reservationId)
                    Log.d("reservationId", "${bookedData.reservationId}")

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

    fun updateData(newItems: List<BookedData>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}