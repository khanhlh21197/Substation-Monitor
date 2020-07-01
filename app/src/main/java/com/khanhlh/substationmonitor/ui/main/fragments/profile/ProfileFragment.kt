package com.khanhlh.substationmonitor.ui.main.fragments.profile


import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.FragmentProfileBinding
import com.khanhlh.substationmonitor.extensions.navigateToActivity
import com.khanhlh.substationmonitor.helper.dialog.alert
import com.khanhlh.substationmonitor.helper.shared_preference.clear
import com.khanhlh.substationmonitor.ui.login.LoginActivity
import com.khanhlh.substationmonitor.ui.main.MainActivity
import com.khanhlh.substationmonitor.utils.USER_PREF
import kotlinx.android.synthetic.main.custom_action_bar.view.*
import kotlinx.android.synthetic.main.fragment_profile.*


/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private lateinit var vm: ProfileViewModel
    lateinit var sharedPref: SharedPreferences

    override fun initView() {
        vm = ProfileViewModel()
        mBinding.vm = vm
        vm.getProfile()
        vm.user.observe(this) { mBinding.user = it }
        includeLayoutListener()
        sharedPref = requireActivity().getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
    }

    private fun includeLayoutListener() {
        actionBar.logout.setOnClickListener {
            alert(
                this,
                R.string.log_out,
                R.string.app_name,
                R.string.cancel,
                R.string.ok,
                null,
                View.OnClickListener {
                    sharedPref.clear(requireActivity(), USER_PREF)
                    FirebaseAuth.getInstance().signOut()
                    requireActivity().finish()
                    requireActivity().navigateToActivity(LoginActivity::class.java)
                })!!.show()
        }
        actionBar.back.setOnClickListener { MainActivity.instance.onBackPressed() }
    }

    override fun getLayoutId(): Int = R.layout.fragment_profile

}
