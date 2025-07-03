package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FramentSearchBinding
import androidx.navigation.fragment.findNavController



class SearchFragment : Fragment() {
    lateinit var binding: FramentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FramentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bookInfo.setOnClickListener {
            val intent = Intent(requireContext(), BookInfoActivity::class.java)
            startActivity(intent)
        }
    }

    //bookinfo눌렀을때 캘린더쪽으로 화면전환
}