package com.khanhlh.substationmonitor.ui.main.fragments.profile


import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.FragmentProfileBinding
import com.khanhlh.substationmonitor.ui.main.MainActivity
import kotlinx.android.synthetic.main.custom_action_bar.view.*
import kotlinx.android.synthetic.main.fragment_profile.*


/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private lateinit var vm: ProfileViewModel

    override fun initView() {
        vm = ProfileViewModel()
        mBinding.vm = vm
        vm.getProfile()
        vm.user.observe(this) { mBinding.user = it }
        includeLayoutListener()
    }

    private fun includeLayoutListener() {
        actionBar.logout.setOnClickListener { FirebaseAuth.getInstance().signOut() }
        actionBar.back.setOnClickListener { MainActivity.instance.onBackPressed() }
    }

    override fun getLayoutId(): Int = R.layout.fragment_profile

}
