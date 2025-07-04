package com.example.sumte

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sumte.databinding.ActivitySearchResultBinding
import java.util.logging.Filter

class SearchResultActivity : AppCompatActivity() {
    lateinit var binding:ActivitySearchResultBinding

//    val priceMin = intent.getIntExtra("price_min", 1000)
//    val priceMax = intent.getIntExtra("price_max", 100000)
//    val people = intent.getStringExtra("people") ?: ""
//    val services = intent.getStringArrayListExtra("services") ?: arrayListOf()
//    val targets = intent.getStringArrayListExtra("targets") ?: arrayListOf()
//    val regions = intent.getStringArrayListExtra("regions") ?: arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchResultAdjustmentsLl.setOnClickListener {
            replaceFragment(FilteringFragment())
        }
        val filterOptions = intent.getParcelableExtra<FilterOptions>("filterOptions")
        filterOptions?.let {
            // 여기서 it을 활용해서 데이터 표시하거나 API 요청 등에 활용
        }
    }

    fun replaceFragment(fragment: FilteringFragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_result_all_cl, fragment)
            .addToBackStack(null).commit()
    }

}