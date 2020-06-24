package com.khanhlh.substationmonitor.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveDataCommunicator : ViewModel() {


    val data=MutableLiveData<Any>()

    fun setCommunicatorData(string: String){
        data.value=string
    }

    fun setCommunicatorData(boolean: Boolean){
        data.value=boolean
    }
}