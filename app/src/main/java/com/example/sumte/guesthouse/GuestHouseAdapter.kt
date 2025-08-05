package com.example.sumte.guesthouse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.R
import com.example.sumte.databinding.ItemGuesthouseBinding

class GuestHouseAdapter(
    private val items: List<GuestHouse>,
    private val viewModel: GuestHouseViewModel,
    private val onItemClick: (GuestHouse) -> Unit
//    private val onHeartClick: (GuestHouse) -> Unit
) : RecyclerView.Adapter<GuestHouseAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: ItemGuesthouseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGuesthouseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guestHouse = items[position]
        with(holder.binding) {
            guesthouseTitleTv.text = guestHouse.title
            guesthouseLocationTv.text = guestHouse.location
            guesthousePriceTv.text = guestHouse.price
            guesthouseIv.setImageResource(guestHouse.imageResId)

            val isLiked = viewModel.isLiked(guestHouse)
            guesthouseHeartIv.setImageResource(
                if (isLiked) R.drawable.heart_home_filled else R.drawable.heart_home_empty
            )

            //클릭 이벤트
            root.setOnClickListener {
                onItemClick(guestHouse)
            }

            guesthouseHeartIv.setOnClickListener {
                viewModel.toggleLike(guestHouse)
//                onHeartClick(guestHouse)
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position)
                }
            }
        }
    }
}

