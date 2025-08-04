package com.example.sumte.myid

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sumte.R
import com.example.sumte.databinding.FragmentMyIdEditBinding

class MyIdEditFragment : Fragment() {

    private var _binding: FragmentMyIdEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMyIdEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = sharedPref.getString("nickname", "닉네임 없음")
        binding.editNickname.setText(nickname)


        binding.editNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""

                if (text.isEmpty()) {
                    showError(true, "닉네임을 입력해 주세요")
                } else if (!text.matches(Regex("^[가-힣a-zA-Z0-9]{2,8}$"))) {
                    showError(true, "닉네임은 2~8자 한글/영문/숫자만 가능합니다.")
                } else {
                    showError(false)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.removeBtn.setOnClickListener {
            binding.editNickname.setText("")
        }
        binding.cancelText.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    fun showError(isError: Boolean, message: String = "") {
        if (isError) {
            binding.editNickname.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.red)
            )
            binding.errorSign.visibility = View.VISIBLE
            binding.errorText.text = message
        } else {
            binding.editNickname.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
            binding.errorSign.visibility = View.GONE
            binding.errorText.text = ""
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
