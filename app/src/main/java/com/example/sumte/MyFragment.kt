package com.example.sumte

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sumte.databinding.FragmentMyBinding
import com.example.sumte.login.LoginActivity
import com.example.sumte.mybook.BookedListActivity
import com.example.sumte.myid.MyIdActivity
import com.example.sumte.review.ReviewManageActivity

class MyFragment : Fragment(){
    lateinit var binding: FragmentMyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = sharedPref.getString("nickname", "닉네임 없음")
        val email = sharedPref.getString("email", "이메일 없음")

        binding.myId.text  = nickname
        binding.myEmail.text = email


        binding.myIdBox.setOnClickListener {
            val intent = Intent(requireContext(), MyIdActivity::class.java)
            startActivity(intent)
        }
        binding.myBookingBox.setOnClickListener {
            val intent = Intent(requireContext(), BookedListActivity::class.java)
            startActivity(intent)
        }
        binding.myReviewBox.setOnClickListener {
            val intent = Intent(requireContext(), ReviewManageActivity::class.java)
            startActivity(intent)
        }

        binding.logoutBtn.setOnClickListener {
            logout()
        }
    }
    private fun logout() {
        requireActivity().getSharedPreferences("auth", AppCompatActivity.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
        val prefs = requireActivity().getSharedPreferences("auth", AppCompatActivity.MODE_PRIVATE)
        val token = prefs.getString("access_token", null)
        Log.d("LogoutCheck", "Token after logout: $token")

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        startActivity(Intent(requireContext(), LoginActivity::class.java))
        fun showToast(message: String) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}