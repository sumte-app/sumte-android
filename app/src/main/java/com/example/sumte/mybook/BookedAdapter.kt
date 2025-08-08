package com.example.sumte.mybook

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ItemBooklistBinding
import com.example.sumte.databinding.ItemHistoryBinding
import com.example.sumte.search.HistoryAdapter
import com.example.sumte.search.HistoryAdapter.HistoryViewHolder

//이미지 일단 제외
class BookedAdapter(
    private val items:List<BookedData>
) : RecyclerView.Adapter<BookedAdapter.BookedViewHolder>() {

    inner class BookedViewHolder(private var binding : ItemBooklistBinding) :
        RecyclerView.ViewHolder(binding.root){
            fun bind(bookedData: BookedData){
                binding.bookedDate.text = bookedData.bookedDate
                binding.dayCount.text = "D-${bookedData.dayCount}"
                binding.roomType.text = bookedData.roomType
                binding.startDate.text = bookedData.startDate
                binding.endDate.text = bookedData.endDate
                binding.dateCount.text = bookedData.dateCount
                binding.adultCount.text = "${bookedData.adultCount}명"
                binding.childCount.text = "${bookedData.childCount}명"
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