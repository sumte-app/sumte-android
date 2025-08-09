package com.example.sumte

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class App : Application(), ViewModelStoreOwner {

    private val appViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore

    override fun onCreate() {
        super.onCreate()
        SharedPreferencesManager.init(this)
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }
}

//package com.example.sumte
//
//import android.app.Application
//import androidx.lifecycle.ViewModelProvider
//
//class App : Application() {
//
//    override fun onCreate() {
//        super.onCreate()
//        SharedPreferencesManager.init(this)
//    }
//}