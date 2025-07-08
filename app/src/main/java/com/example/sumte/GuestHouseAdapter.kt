package com.example.sumte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sumte.databinding.ItemGuesthouseBinding

class GuestHouseAdapter(
    private val items: List<GuestHouse>
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
            title.text = guestHouse.title
            location.text = guestHouse.location
            price.text = guestHouse.price
            houseIv.setImageResource(guestHouse.imageResId)
        }
    }
}

