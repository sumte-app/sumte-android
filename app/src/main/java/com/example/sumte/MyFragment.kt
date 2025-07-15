package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentMyBinding

class MyFragment : Fragment(){
    lateinit var binding: FragmentMyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.myIdBox.setOnClickListener {
            val intent = Intent(requireContext(), MyIdActivity::class.java)
            startActivity(intent)
        }
        binding.myBookingBox.setOnClickListener {
            val intent = Intent(requireContext(), BookedListActivity::class.java)
            startActivity(intent)
        }

    }
}