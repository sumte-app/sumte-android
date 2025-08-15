package com.example.sumte.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.R
import com.example.sumte.databinding.ItemHistoryBinding
//검색기록 어댑터
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
            } else {
                binding.childCount.visibility = View.GONE
            }

            binding.historyItem.setOnClickListener {
                val clickedHistory = items[adapterPosition]
                // 중복 검색 시 처리 로직
                items.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                items.add(0, clickedHistory)
                notifyItemInserted(0)
                saveHistory(items)

                // 검색결과로 데이터전달
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
            //검색기록 단일 삭제
            binding.deleteBtn.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    items.removeAt(position)
                    notifyItemRemoved(position)
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
    //히스토리 추가
    fun addItem(item: History) {
        items.add(0, item)
        notifyItemInserted(0)
        saveHistory(items)
    }
    //히스토리 제거
    fun removeItem(item: History) {
        val index = items.indexOf(item)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
            saveHistory(items)
        }
    }
    //히스토리 개수 제한
    fun trimToMaxSize(max: Int) {
        while (items.size > max) {
            val removed = items.removeAt(items.size - 1)
            notifyItemRemoved(items.size)
        }
        saveHistory(items)
    }
    //히스토리 전부 삭제
    fun clearAll() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
        saveHistory(items)  // SharedPreferences에 빈 리스트 저장
    }


}
