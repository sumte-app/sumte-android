package com.example.sumte.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val items: List<History>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: History) {
            binding.houseName.text = history.houseName
            binding.startDate.text = history.startDate
            binding.endDate.text = history.endDate
            binding.adultCount.text = "성인 ${history.adultCount}"

            // 아동 수가 0이면 childCount와 컴마 숨기기
            if (history.childCount > 0) {
                binding.childCount.visibility = View.VISIBLE
                binding.childCount.text = "아동 ${history.childCount}"
                binding.comma.visibility = View.VISIBLE
            } else {
                binding.childCount.visibility = View.GONE
                binding.comma.visibility = View.GONE
            }
//            // endDate가 비어있으면 dateComma 숨기기
//            binding.dateComma.visibility = if (history.endDate.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
