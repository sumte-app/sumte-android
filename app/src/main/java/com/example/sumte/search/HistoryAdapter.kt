package com.example.sumte.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.R
import com.example.sumte.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val items: MutableList<History>,
    private val saveHistory: (List<History>) -> Unit,
    private val onEmptyList: () -> Unit
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

            binding.historyItem.setOnClickListener {
                val clickedHistory = items[adapterPosition]

                // 기존 위치에서 제거
                items.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)

                // 맨 앞으로 추가
                items.add(0, clickedHistory)
                notifyItemInserted(0)

                // SharedPreferences 저장 콜백 호출
                saveHistory(items)

                // 화면 전환
                val fragment = SearchResultFragment().apply {
                    arguments = Bundle().apply {
                        putString(BookInfoActivity.EXTRA_KEYWORD, clickedHistory.keyword)
                        putString("startDate", clickedHistory.startDate)
                        putString("endDate", clickedHistory.endDate)
                        putInt("adultCount", clickedHistory.adultCount)
                        putInt("childCount", clickedHistory.childCount)
                    }
                }
                (binding.root.context as? FragmentActivity)?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.book_info_container, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
            }

            binding.deleteBtn.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    items.removeAt(position)
                    notifyItemRemoved(position)

                    // SharedPreferences에 저장하는 콜백 호출
                    saveHistory(items)
                    if (items.isEmpty()) {
                        onEmptyList()
                    }
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

    fun contains(item: History): Boolean = items.contains(item)


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
            saveHistory(items)
        }
    }

    fun trimToMaxSize(max: Int) {
        while (items.size > max) {
            val removed = items.removeAt(items.size - 1)
            notifyItemRemoved(items.size)
        }
        saveHistory(items)
    }

    fun clearAll() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
        saveHistory(items)  // SharedPreferences에 빈 리스트 저장
    }


}
