package com.khanhlh.substationmonitor.base


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.khanhlh.substationmonitor.di.components.DaggerViewModelInjector
import com.khanhlh.substationmonitor.di.components.ViewModelInjector
import com.khanhlh.substationmonitor.di.modules.NetworkModule
import com.khanhlh.substationmonitor.ui.login.LoginActivityViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.home.HomeViewModel
import com.khanhlh.substationmonitor.ui.register.RegisterActivityViewModel


abstract class BaseViewModel<T> : ViewModel() {

    var loadingVisibility = MutableLiveData<Int>()

    var errorMessage: MutableLiveData<String> = MutableLiveData()

    var view: T? = null

    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .networkModule(NetworkModule)
        .build()

    fun attachView(view: T) {
        this.view = view
    }

    fun detachView() {
        this.view = null
    }

    init {
        inject()
    }

    private fun inject() {
        when (this) {
            is LoginActivityViewModel -> injector.inject(this)
            is RegisterActivityViewModel -> injector.inject(this)
            is HomeViewModel -> injector.inject(this)
        }
    }
}

