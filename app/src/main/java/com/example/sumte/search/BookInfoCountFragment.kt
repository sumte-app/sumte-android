package com.example.sumte.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.sumte.App
import com.example.sumte.R
import com.example.sumte.common.bindBookInfoUI
import com.example.sumte.common.getBookInfoViewModel
import com.example.sumte.databinding.FragmentBookInfoCountBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class BookInfoCountFragment : Fragment() {
    lateinit var binding: FragmentBookInfoCountBinding
    private val viewModel by lazy { getBookInfoViewModel() }
    private var source: String? = null
    private var maxPeople: Int? = null

    private fun updateApplyButton(adultCount: Int, childCount: Int) {
        val total = adultCount + childCount
        binding.applyBtn.isEnabled = maxPeople?.let { total <= it } ?: true
        binding.applyBtn.alpha = if (binding.applyBtn.isEnabled) 1f else 0.5f
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBookInfoCountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        source = activity?.intent?.getStringExtra(BookInfoActivity.EXTRA_SOURCE)
        Log.d("BookInfoCountFragment","${source}")
        maxPeople = arguments?.getInt("maxPeople")?.takeIf { it > 0 }
        Log.d("BookInfoCountFragment", "maxPeople: $maxPeople")

        bindBookInfoUI(binding, viewModel)

        var adultCount = viewModel.adultCount
        var childCount = viewModel.childCount
        binding.adultCountNum.text = adultCount.toString()
        binding.childCountNum.text = childCount.toString()

        //버튼이미지세팅
        binding.adultMinusBtn.setImageResource(
            if (adultCount > 1) R.drawable.minus_green else R.drawable.minus_gray
        )
        binding.childMinusBtn.setImageResource(
            if (childCount > 0) R.drawable.minus_green else R.drawable.minus_gray
        )

        //숫자변화
        binding.adultPlusBtn.setOnClickListener {
            adultCount++
            //viewModel.adultCount = adultCount
            if (adultCount > 1 ){
                binding.adultMinusBtn.setImageResource(R.drawable.minus_green)
            }
            binding.adultCountNum.text = adultCount.toString()
            binding.adultCount.text = String.format("성인 %d", adultCount)
        }
        binding.adultMinusBtn.setOnClickListener {
            if (adultCount > 1) {
                adultCount--
                binding.adultCountNum.text = adultCount.toString()
            }
            if (adultCount == 1) {
                binding.adultMinusBtn.setImageResource(R.drawable.minus_gray)
            }
            //viewModel.adultCount = adultCount
            binding.adultCount.text = String.format("성인 %d", adultCount)
        }
        binding.childPlusBtn.setOnClickListener {
            childCount++
            //viewModel.childCount = childCount
            if (childCount > 0) {
                binding.childMinusBtn.setImageResource(R.drawable.minus_green)
                binding.countComma.visibility = View.VISIBLE
            }
            binding.childCountNum.text = childCount.toString()
            binding.childCount.text = String.format("아동 %d", childCount)
        }
        binding.childMinusBtn.setOnClickListener {
            if (childCount > 0) {
                childCount--
                binding.childCountNum.text = childCount.toString()
                binding.childCount.text = String.format("아동 %d", childCount)
            }
            if (childCount == 0){
                binding.childMinusBtn.setImageResource(R.drawable.minus_gray)
                binding.childCount.text = null
                binding.countComma.visibility = View.GONE
            }
            //viewModel.childCount = childCount
        }

        binding.calendar.setOnClickListener {
            viewModel.adultCount = adultCount
            viewModel.childCount = childCount
            val fragment = BookInfoDateFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.book_info_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.applyBtn.setOnClickListener {
            viewModel.adultCount = adultCount
            viewModel.childCount = childCount
            (binding.root.context as? BookInfoActivity)?.onApplyClicked()

        }
        binding.cancelBtn.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            if (fragmentManager.backStackEntryCount > 0) {
                // 같은 액티비티의 이전 프래그먼트로 돌아감
                fragmentManager.popBackStack()
            } else {
                // 다른 액티비티에서 왔다면 현재 액티비티 종료
                requireActivity().finish()
            }
        }

    }

}