package com.khanhlh.substationmonitor.di

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.ui.login.LoginActivityViewModel
import com.khanhlh.substationmonitor.ui.main.MainActivity
import com.khanhlh.substationmonitor.ui.main.MainViewModel
import com.khanhlh.substationmonitor.ui.register.RegisterActivityViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val activity: AppCompatActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {

            modelClass.isAssignableFrom(LoginActivityViewModel::class.java) -> {
                LoginActivityViewModel(MyApp()) as T
            }
            modelClass.isAssignableFrom(RegisterActivityViewModel::class.java) -> {
                RegisterActivityViewModel(MyApp()) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(MyApp()) as T
            }
            else -> throw IllegalArgumentException("Unknown class name")
        }
    }
}