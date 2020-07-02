package com.khanhlh.substationmonitor.ui.main.fragments.detail

import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.DetailDeviceFragmentBinding

class DetailDeviceFragment : BaseFragment<DetailDeviceFragmentBinding>() {
    companion object {
        const val ID_DEVICE = "ID_DEVICE"
        const val TAG = "DetailDeviceFragment"
    }

    lateinit var vm: DetailDeviceViewModel
    override fun initView() {
        vm = DetailDeviceViewModel()
        mBinding.vm = vm
        getBundleData()
    }

    private fun getBundleData() {
        val args = arguments
        val idDevice = args!!.getString(ID_DEVICE)
        vm.observerDevice(idDevice as String)
    }

    override fun getLayoutId(): Int = R.layout.detail_device_fragment
}