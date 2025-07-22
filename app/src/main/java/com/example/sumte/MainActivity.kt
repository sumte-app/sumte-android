package com.example.sumte

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sumte.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        initBottomNavigation()

        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.findFragmentById(R.id.main_container)

            binding.bottomNavView.visibility =
                if (current is SearchFragment) View.GONE else View.VISIBLE

            val targetId = when (current) {
                is HomeFragment    -> R.id.fragment_home
                is LikeFragment -> R.id.fragment_favorite
                is MyFragment -> R.id.fragment_my
                else               -> return@addOnBackStackChangedListener
            }
            binding.bottomNavView.menu.findItem(targetId).isChecked = true
        }





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
                    navigateToSearchFragment()
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
    fun navigateToSearchFragment() {
        val fragment = SearchFragment()
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()

    }



}