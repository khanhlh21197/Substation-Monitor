package com.khanhlh.substationmonitor.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.khanhlh.substationmonitor.base.BaseViewModel

class MainViewModel() : BaseViewModel<Any?>() {
    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title
    val onFabClick: MutableLiveData<Boolean> = MutableLiveData(false)

    fun updateActionBarTitle(title: String) = _title.postValue(title)
    fun onFabClickListener(_onFabClick: Boolean) {
        onFabClick.value = _onFabClick
    }

}