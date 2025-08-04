package com.example.sumte

import android.app.Application
import androidx.lifecycle.ViewModelProvider

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPreferencesManager.init(this)
    }
}