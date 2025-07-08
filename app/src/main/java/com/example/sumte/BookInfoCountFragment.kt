package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentBookInfoCountBinding

class BookInfoCountFragment : Fragment() {
    lateinit var binding: FragmentBookInfoCountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBookInfoCountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var adultCount = binding.adultCount.text.toString().toInt()
        var childCount = binding.childCount.text.toString().toInt()

        binding.adultPlusBtn.setOnClickListener {
            adultCount++
            if (adultCount > 1 ){
                binding.adultMinusBtn.setImageResource(R.drawable.minus_green)
            }
            binding.adultCount.text = adultCount.toString()
            binding.adultCountText.text = String.format("성인 %d", adultCount)
        }
        binding.adultMinusBtn.setOnClickListener {
            if (adultCount > 1) {
                adultCount--
                binding.adultCount.text = adultCount.toString()
            }
            if (adultCount == 1) {
                binding.adultMinusBtn.setImageResource(R.drawable.minus_gray)
            }
            binding.adultCountText.text = String.format("성인 %d", adultCount)
        }
        binding.childPlusBtn.setOnClickListener {
            childCount++
            if (childCount > 0) {
                binding.childMinusBtn.setImageResource(R.drawable.minus_green)
            }
            binding.childCount.text = childCount.toString()
            binding.childCountText.text = String.format(", 아동 %d", childCount)
        }
        binding.childMinusBtn.setOnClickListener {
            if (childCount > 0) {
                childCount--
                binding.childCount.text = childCount.toString()
                binding.childCountText.text = String.format(", 아동 %d", childCount)
            }
            if (childCount == 0){
                binding.childMinusBtn.setImageResource(R.drawable.minus_gray)
                binding.childCountText.text = null
            }
        }

        binding.applyBtn.setOnClickListener {
            val intent = Intent(requireContext(), SearchResultActivity::class.java)
            startActivity(intent)
        }

    }

}