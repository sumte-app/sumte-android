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
import androidx.fragment.app.viewModels
import com.example.sumte.R
import com.example.sumte.databinding.FragmentMyIdEditBinding

class MyIdEditFragment : Fragment() {

    private var _binding: FragmentMyIdEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyIdViewModel by viewModels()


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
        val originalNickname = sharedPref.getString("nickname", "닉네임 없음") ?: "닉네임 없음"
        binding.editNickname.setText(originalNickname)

        binding.editNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString() ?: ""

                when {
                    input.isEmpty() -> {
                        showError(true, "닉네임을 입력해 주세요")
                    }

                    !input.matches(Regex("^[가-힣a-zA-Z0-9]{2,8}$")) -> {
                        showError(true, "닉네임은 2~8자 한글/영문/숫자만 가능합니다.")
                    }

                    input == originalNickname -> {
                        showError(true, "동일한 닉네임입니다")
                    }

                    else -> {
                        showError(false)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.updateResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                // 1. SharedPreferences에 새 닉네임 저장
                val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                sharedPref.edit().putString("nickname", binding.editNickname.text.toString()).apply()

                // 2. 뒤로 가기 (수정 완료)
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                // 실패했을 경우 에러 메시지 표시
                showError(true, "닉네임 변경에 실패했습니다. 다시 시도해 주세요.")
            }
        }


        binding.removeBtn.setOnClickListener {
            binding.editNickname.setText("")
        }

        binding.cancelText.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.applyBox.setOnClickListener {
            val newNickname = binding.editNickname.text.toString()

            if (newNickname != originalNickname && binding.applyBox.isEnabled) {
                viewModel.updateNickname(newNickname)
            }
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
        binding.applyBox.isEnabled = !isError
        binding.applyBox.alpha = if (!isError) 1f else 0.5f
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
