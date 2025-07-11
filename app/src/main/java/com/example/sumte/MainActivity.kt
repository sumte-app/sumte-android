package com.example.sumte

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sumte.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        initBottomNavigation()

//        SearchResultAcitivty 실행용
//        val intent = Intent(this, SearchResultActivity::class.java)
//        startActivity(intent)
    }

    private fun initBottomNavigation(){
        supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeFragment())
            .commitAllowingStateLoss()

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeFragment()).commitAllowingStateLoss()
                    true
                }
                R.id.fragment_search -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, SearchFragment()).commitAllowingStateLoss()
                    true
                }
                R.id.fragment_favorite -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, LikeFragment()).commitAllowingStateLoss()
                    true
                }
                R.id.fragment_my -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, MyFragment()).commitAllowingStateLoss()
                    true
                }
                else -> false
            }
        }
    }
    fun selectBottomNavItem(itemId: Int) {
        binding.bottomNavView.selectedItemId = itemId
    }
}