//package com.example.sumte.housedetail
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import androidx.viewpager2.widget.ViewPager2
//import com.example.sumte.databinding.ItemRoomDetailInfoBinding
//
//class RoomDetailInfoAdapter(
//    private var items: List<RoomDetailInfo> = emptyList(),
//    private val onImageClick: (String) -> Unit = {}
//) : RecyclerView.Adapter<RoomDetailInfoAdapter.VH>() {
//
//    inner class VH(val binding: ItemRoomDetailInfoBinding) : RecyclerView.ViewHolder(binding.root) {
//        private var pageCallback: ViewPager2.OnPageChangeCallback? = null
//        private val imageAdapter = HouseImageAdapter(onClick = onImageClick)
//
//        fun bind(item: RoomDetailInfo) = with(binding) {
//            tvRoomName.text = item.name
//            tvRoomPrice.text = "${String.format("%,d", item.price)}원"
//            tvPeopleInfo1.text = "기준인원 ${item.standardCount}인"
//            tvPeopleInfo2.text = "(정원 ${item.totalCount}인)"
//            tvCheckInOutText.text = "체크인 ${trimSec(item.checkin)} · 체크아웃 ${trimSec(item.checkout)}"
//            tvContent.text = item.content
//
//            // 이미지 슬라이더
//            val urls = item.imageUrls.map { it.trim() }.filter { it.isNotEmpty() }
//            vpImages.adapter = imageAdapter
//            imageAdapter.submitList(urls) {
//                updateIndicator(if (urls.isEmpty()) 0 else 1, urls.size)
//                if (urls.isNotEmpty()) vpImages.setCurrentItem(0, false)
//            }
//
//            // 기존 콜백 해제 후 새로 등록 (메모리/이벤트 누수 방지)
//            pageCallback?.let { vpImages.unregisterOnPageChangeCallback(it) }
//            pageCallback = object : ViewPager2.OnPageChangeCallback() {
//                override fun onPageSelected(position: Int) {
//                    updateIndicator(position + 1, imageAdapter.itemCount)
//                }
//            }.also { vpImages.registerOnPageChangeCallback(it) }
//        }
//
//        private fun updateIndicator(current: Int, total: Int) {
//            binding.tvImageIndicator.text = "$current / $total"
//        }
//
//        fun clear() {
//            pageCallback?.let { binding.vpImages.unregisterOnPageChangeCallback(it) }
//            pageCallback = null
//            binding.vpImages.adapter = null
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
//        val binding = ItemRoomDetailInfoBinding.inflate(
//            LayoutInflater.from(parent.context), parent, false
//        )
//        return VH(binding)
//    }
//
//    override fun onBindViewHolder(holder: VH, position: Int) {
//        holder.bind(items[position])
//    }
//
//    override fun onViewRecycled(holder: VH) {
//        holder.clear()
//        super.onViewRecycled(holder)
//    }
//
//    override fun getItemCount(): Int = items.size
//
//    /** 보통 상세 페이지는 1개만 쓰므로 이 헬퍼 제공 */
//    fun submit(detail: RoomDetailInfo) {
//        items = listOf(detail)
//        notifyDataSetChanged()
//    }
//
//    fun submitList(newItems: List<RoomDetailInfo>) {
//        items = newItems
//        notifyDataSetChanged()
//    }
//}
//
//private fun trimSec(s: String): String {
//    return if (s.count { it == ':' } == 2 && s.endsWith(":00")) s.dropLast(3) else s
//}
