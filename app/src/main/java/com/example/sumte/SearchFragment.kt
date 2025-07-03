package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FramentSearchBinding

class SearchFragment : Fragment() {
    lateinit var binding: FramentSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FramentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    //bookinfo눌렀을때 캘린더쪽으로 화면전환
}