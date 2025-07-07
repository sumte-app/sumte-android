package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentMyIdMainBinding

class MyIdMainFragment : Fragment(){
    lateinit var binding: FragmentMyIdMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMyIdMainBinding.inflate(inflater, container, false)
        return binding.root
    }
}