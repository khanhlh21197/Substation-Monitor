package com.khanhlh.substationmonitor.di.components

import com.khanhlh.substationmonitor.di.modules.NetworkModule
import com.khanhlh.substationmonitor.ui.login.LoginActivityViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.home.HomeViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.profile.ProfileViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.room.RoomViewModel
import com.khanhlh.substationmonitor.ui.register.RegisterActivityViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(NetworkModule::class)])
interface ViewModelInjector {

    fun inject(loginActivity: LoginActivityViewModel)
    fun inject(registerActivityViewModel: RegisterActivityViewModel)
    fun inject(homeViewModel: HomeViewModel)
    fun inject(profileViewModel: ProfileViewModel)
    fun inject(roomViewModel: RoomViewModel)


    @Component.Builder
    interface Builder {

        fun build(): ViewModelInjector
        fun networkModule(networkModule: NetworkModule): Builder
    }

}