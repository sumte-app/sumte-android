package com.example.sumte.housedetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.R
import com.example.sumte.databinding.ItemHouseImageBinding

class HouseImageAdapter(
    private var urls: List<String>
) : RecyclerView.Adapter<HouseImageAdapter.VH>() {

    fun submit(newUrls: List<String>) {
        urls = newUrls
        notifyDataSetChanged()
    }

    override fun getItemCount() = urls.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_house_image, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val url = urls[position]
        Glide.with(holder.itemView)
            .load(url)
            .placeholder(R.drawable.sample_house1)
            .error(R.drawable.sample_house1)
            .into(holder.img)
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.ivImage)
    }
}
