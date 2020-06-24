package com.khanhlh.substationmonitor.ui.main.fragments.home


import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.FragmentHomeBinding


class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    lateinit var vm: HomeViewModel

    override fun initView() {
        vm = HomeViewModel()
        mBinding.viewModel = vm
        vm.getAllUsers()
    }

    override fun getLayoutId(): Int = R.layout.fragment_home

}
