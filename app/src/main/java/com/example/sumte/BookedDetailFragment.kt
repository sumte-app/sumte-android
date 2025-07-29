package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentBookedDetailBinding

class BookedDetailFragment : Fragment() {
    lateinit var binding: FragmentBookedDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentBookedDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        binding.cancelBtn.setOnClickListener {
//            binding.popupOverlay.visibility = View.VISIBLE
//        }
//    }

}