package com.example.sumte.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val items: MutableList<History>,
    private val saveHistory: (List<History>) -> Unit   // 저장 콜백 받음
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: History) {
            binding.keyword.text = history.keyword
            binding.startDate.text = history.startDate
            binding.endDate.text = history.endDate
            binding.adultCount.text = "성인 ${history.adultCount}"

            if (history.childCount > 0) {
                binding.childCount.visibility = View.VISIBLE
                binding.childCount.text = "아동 ${history.childCount}"
                binding.comma.visibility = View.VISIBLE
            } else {
                binding.childCount.visibility = View.GONE
                binding.comma.visibility = View.GONE
            }

            binding.deleteBtn.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    items.removeAt(position)
                    notifyItemRemoved(position)

                    // SharedPreferences에 저장하는 콜백 호출
                    saveHistory(items)
                }
            }
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


    fun addItem(item: History) {
        items.add(0, item)        // 리스트 맨 앞에 새 아이템 추가
        notifyItemInserted(0)     // 0번 인덱스에 아이템 추가 알림
        saveHistory(items)        // 변경된 리스트 저장 콜백 호출
    }

    fun removeItem(item: History) {
        val index = items.indexOf(item)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    fun clearAll() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
        saveHistory(items)  // SharedPreferences에 빈 리스트 저장
    }


}
