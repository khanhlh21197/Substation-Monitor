package com.khanhlh.substationmonitor.base


import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.di.components.DaggerViewModelInjector
import com.khanhlh.substationmonitor.di.components.ViewModelInjector
import com.khanhlh.substationmonitor.di.modules.NetworkModule
import com.khanhlh.substationmonitor.ui.login.LoginActivityViewModel
import com.khanhlh.substationmonitor.ui.main.MainViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.detail.DetailDeviceViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.device.DeviceViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.home.HomeViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.profile.ProfileViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.room.RoomViewModel
import com.khanhlh.substationmonitor.ui.register.RegisterActivityViewModel


abstract class BaseViewModel<T>() : ViewModel() {

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
            is ProfileViewModel -> injector.inject(this)
            is RoomViewModel -> injector.inject(this)
            is DetailDeviceViewModel -> injector.inject(this)
            is DeviceViewModel -> injector.inject(this)
            is MainViewModel -> injector.inject(this)
        }
    }
}

