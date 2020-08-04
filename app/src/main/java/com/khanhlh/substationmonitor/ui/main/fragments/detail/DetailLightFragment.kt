package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.os.Handler
import android.view.View
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.DetailLightFragBinding
import com.yanzhenjie.wheel.OnWheelChangedListener
import com.yanzhenjie.wheel.OnWheelScrollListener
import com.yanzhenjie.wheel.WheelView
import kotlinx.android.synthetic.main.detail_light_frag.*


class DetailLightFragment : BaseFragment<DetailLightFragBinding, DetailDeviceViewModel>(),
    View.OnClickListener {
    private var mAutoIncrement = false
    private var mAutoDecrement = false
    private var wheelScrolled = false
    val repeatUpdateHandler = Handler()

    companion object {
        const val ID_DEVICE = "ID_DEVICE"
        const val TAG = "DetailDeviceFragment"
        const val REP_DELAY = 1L
    }

    override fun initView() {
        vm = DetailDeviceViewModel()
        mBinding.vm = vm
        initWheelAdapter()
        onSwitchChange()
    }

    private fun initWheelAdapter() {
        val wheelMenu1 = IntArray(24) { it + 1 }
        val wheelMenu2 = IntArray(60) { it + 1 }
    }

    private fun onSwitchChange() {
        if (lightSwitch.isChecked) {
            lightOn.visibility = View.VISIBLE
            lightOff.visibility = View.GONE
        } else {
            lightOn.visibility = View.GONE
            lightOff.visibility = View.VISIBLE
        }

        lightSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lightOn.visibility = View.VISIBLE
                lightOff.visibility = View.GONE
            } else {
                lightOn.visibility = View.GONE
                lightOff.visibility = View.VISIBLE
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.detail_light_frag
    }

    override fun onClick(v: View?) {

    }

    // Wheel scrolled listener
    var scrolledListener: OnWheelScrollListener = object : OnWheelScrollListener {
        override fun onScrollingFinished(wheel: WheelView?) {
            wheelScrolled = false
        }

        override fun onScrollingStarted(wheel: WheelView?) {
            wheelScrolled = true
        }
    }

    // Wheel changed listener
    private val changedListener =
        OnWheelChangedListener { wheel, oldValue, newValue ->
            if (!wheelScrolled) {
                updateStatus()
            }
        }
}