package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.example.sumte.databinding.FragmentMyIdEditBinding

class MyIdEditFragment : Fragment(){
    lateinit var binding: FragmentMyIdEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMyIdEditBinding.inflate(inflater, container, false)
        return binding.root
    }

}