package com.example.sumte.myid

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sumte.R
import com.example.sumte.databinding.FragmentMyIdMainBinding
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

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
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = sharedPref.getString("nickname", "닉네임 없음")
        val email = sharedPref.getString("email", "이메일 없음")

        binding.myId.text = nickname
        binding.myEmail.text = email


        binding.editBtn.setOnClickListener {
            val fragment = MyIdEditFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.my_id_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

}