package com.example.sumte

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sumte.databinding.ActivitySearchResultBinding
import java.util.logging.Filter

class SearchResultActivity : AppCompatActivity() {
    lateinit var binding:ActivitySearchResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        binding.searchResultAdjustmentsLl.setOnClickListener {
            replaceFragment(FilteringFragment())
        }
    }
    fun replaceFragment(fragment: FilteringFragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_result_all_cl, fragment)
            .addToBackStack(null).commit()
    }

}