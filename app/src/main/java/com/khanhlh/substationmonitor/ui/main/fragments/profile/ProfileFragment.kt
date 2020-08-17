package com.khanhlh.substationmonitor.ui.main.fragments.profile


import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.FragmentProfileBinding
import com.khanhlh.substationmonitor.extensions.fromJson
import com.khanhlh.substationmonitor.extensions.getMacAddr
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.extensions.navigateToActivity
import com.khanhlh.substationmonitor.helper.dialog.alert
import com.khanhlh.substationmonitor.helper.shared_preference.clear
import com.khanhlh.substationmonitor.model.ThietBiResponse
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import com.khanhlh.substationmonitor.ui.login.LoginActivity
import com.khanhlh.substationmonitor.ui.main.MainActivity
import com.khanhlh.substationmonitor.utils.USER_PREF
import kotlinx.android.synthetic.main.custom_action_bar.view.*
import kotlinx.android.synthetic.main.fragment_profile.*


/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>() {
    lateinit var sharedPref: SharedPreferences
    private lateinit var mqttHelper: MqttHelper
    private lateinit var gson: Gson
    private lateinit var macAddress: String

    override fun onFabClick() = toast(getTitle())

    override fun initView() {
        vm = ProfileViewModel(MyApp())
        mBinding.vm = vm

        includeLayoutListener()
        sharedPref = requireActivity().getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()
        initMqtt()
    }

    private fun initMqtt() {
        macAddress = getMacAddr()!!

        gson = Gson()
        mqttHelper = MqttHelper(requireActivity())

        macAddress.let {
            mqttHelper.connect(it, messageCallBack = object : MqttHelper.MessageCallBack {
                override fun onSuccess(message: String) {
                    val response = fromJson<ThietBiResponse>(message)
                    if ("0" == response.errorCode && "true" == response.result) {

                    } else {
                        toast(R.string.server_error)
                    }
                }

                override fun onError(error: Throwable) {
                    toast(error.toString())
                }
            })
        }
    }

    private fun publishMessage(topic: String, json: String) {
        mqttHelper.isConnected.observe(this, Observer<Boolean> {
            if (it) {
                vm.hideLoading()
                mqttHelper.publishMessage(topic, json)
                    .subscribe({ logD(it.toString()) }, { logD(it.toString()) })
            } else {
                vm.showLoading()
            }
        })
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
        actionBar.back.setOnClickListener { MainActivity.getInstance().onBackPressed() }
    }

    override fun getLayoutId(): Int = R.layout.fragment_profile
    override fun getTitle(): String {
        return "ProfileFragment"
    }

}
