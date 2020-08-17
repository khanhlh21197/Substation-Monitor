package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.view.View
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.DetailTempFragBinding

class DetailTempFrag : BaseFragment<DetailTempFragBinding, DetailDeviceViewModel>() {
    companion object {
        const val ID_DEVICE = "ID_DEVICE"
        const val TAG = "DetailDeviceFragment"
    }

    override fun onFabClick() = toast(getTitle())

    override fun initView() {
        vm = DetailDeviceViewModel(MyApp())
        mBinding.vm = vm
        getBundleData()
    }

    private fun getBundleData() {
        val args = arguments
        val idDevice = args!!.getString(ID_DEVICE)
        vm.observerDevice(idDevice as String)
    }

    override fun getLayoutId(): Int = R.layout.detail_temp_frag
    override fun getTitle(): String {
        return "DetailTempFragment"
    }
}