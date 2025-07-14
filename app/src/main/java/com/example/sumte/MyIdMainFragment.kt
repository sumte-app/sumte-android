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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.editBtn.setOnClickListener {
            val fragment = MyIdEditFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.my_id_container, fragment)
                .commit()
        }
    }
}