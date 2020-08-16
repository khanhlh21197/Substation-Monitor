package com.khanhlh.substationmonitor.ui.main

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title
    private val _onFabClick = MutableLiveData<View.OnClickListener>()
    val onFabClick: LiveData<View.OnClickListener>
        get() = _onFabClick

    fun updateActionBarTitle(title: String) = _title.postValue(title)
    fun onFabClickListener(onFabClick: View.OnClickListener) = _onFabClick.postValue(onFabClick)

}