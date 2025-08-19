package com.example.sumte.review
import kotlin.collections.get



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sumte.R

import com.example.sumte.databinding.ItemReviewCardBinding


class ReviewCardAdapter(
    private val onItemClick: ((ReviewItem) -> Unit)? = null
) : ListAdapter<ReviewItem, ReviewCardAdapter.VH>(diff) {

    inner class VH(val binding: ItemReviewCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReviewItem) = with(binding) {

            tvDate.text = formatIsoToDate(item.createdAt)



            setStars(item.score)


            tvReview.text = item.contents

            // 이미지 (첫 장만)
            val thumb = item.imageUrls?.firstOrNull()
            if (thumb.isNullOrBlank()) {
                ivImage.setImageResource(R.drawable.sample_room1) // 기본 이미지
            } else {
                Glide.with(ivImage)
                    .load(thumb)
                    .placeholder(R.drawable.sample_room1)
                    .error(R.drawable.sample_room1)
                    .into(ivImage)
            }

            root.setOnClickListener { onItemClick?.invoke(item) }
        }

        private fun ItemReviewCardBinding.setStars(score: Int) {
            // score: 0~5 범위로 클램핑
            val s = score.coerceIn(0, 5)


            val filled = R.drawable.star_fill
            val empty  = R.drawable.star

            val stars = listOf(tvStar1, tvStar2, tvStar3, tvStar4, tvStar5)
            stars.forEachIndexed { idx, iv ->
                iv.setImageResource(if (idx < s) filled else empty)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        val binding = ItemReviewCardBinding.inflate(inf, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatIsoToDate(raw: String): String {
        val out = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val zone = java.time.ZoneId.systemDefault()

        // 1) Instant/Offset(‘Z’ 또는 +09:00) 포함
        runCatching {
            return out.format(java.time.Instant.parse(raw).atZone(zone))
        }
        runCatching {
            val odt = java.time.OffsetDateTime.parse(raw, java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            return out.format(odt.atZoneSameInstant(zone))
        }

        // 2) 로컬 날짜-시간(오프셋 없음) "2025-07-26T09:29:03.280294"
        runCatching {
            val ldt = java.time.LocalDateTime.parse(raw, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return out.format(ldt)
        }

        // 3) "2025-07-26 09:29:03" 같이 공백 구분 or 앞 10자리만 날짜일 때
        runCatching {
            val datePart = raw.take(10) // "YYYY-MM-DD"
            val d = java.time.LocalDate.parse(datePart, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            return out.format(d)
        }

        // 4) 마지막 안전망: 보이는 형태라도 "YYYY.MM.DD" 비슷하게
        return raw.take(10).replace('-', '.')
    }


    companion object {
        private val diff = object : DiffUtil.ItemCallback<ReviewItem>() {
            override fun areItemsTheSame(old: ReviewItem, new: ReviewItem) = old.id == new.id
            override fun areContentsTheSame(old: ReviewItem, new: ReviewItem) = old == new
        }
    }
}
