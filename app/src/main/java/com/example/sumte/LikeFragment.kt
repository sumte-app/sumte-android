package com.example.sumte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumte.databinding.FragmentLikeBinding
import kotlinx.coroutines.launch

class LikeFragment : Fragment() {
    lateinit var binding: FragmentLikeBinding
    private lateinit var viewModel: GuestHouseViewModel
    private lateinit var adapter: LikeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[GuestHouseViewModel::class.java]
        viewModel.likedList.observe(viewLifecycleOwner) { likedItems ->
            adapter = LikeAdapter(likedItems.toMutableList(), viewModel)
            binding.likeRv.layoutManager = LinearLayoutManager(requireContext())
            binding.likeRv.adapter = adapter
        }
    }
}