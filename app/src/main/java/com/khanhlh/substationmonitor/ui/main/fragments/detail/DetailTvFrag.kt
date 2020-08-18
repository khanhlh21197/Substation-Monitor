package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.annotation.SuppressLint
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.DetailTvFragBinding
import kotlinx.android.synthetic.main.detail_ac_frag.*


class DetailTvFrag : BaseFragment<DetailTvFragBinding, DetailDeviceViewModel>(),
    View.OnClickListener {
    private var mAutoIncrement = false
    private var mAutoDecrement = false
    val repeatUpdateHandler = Handler()

    override fun onFabClick() = toast(getTitle())

    companion object {
        const val ID_DEVICE = "ID_DEVICE"
        const val TAG = "DetailDeviceFragment"
        const val REP_DELAY = 1L
    }

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

    override fun getLayoutId(): Int = R.layout.detail_tv_frag
    override fun onClick(v: View?) {

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onLongClick() {
        plus.setOnLongClickListener {
            mAutoIncrement = true
            repeatUpdateHandler.post(RptUpdater())
            true
        }

        plus.setOnTouchListener { v, event ->
            if ((event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
                && mAutoIncrement
            ) mAutoIncrement = false
            false
        }

        minus.setOnTouchListener { v, event ->
            if ((event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
                && mAutoDecrement
            ) mAutoDecrement = false
            false
        }

        minus.setOnLongClickListener {
            mAutoDecrement = true
            repeatUpdateHandler.post(RptUpdater())
            true
        }
    }

    inner class RptUpdater : Runnable {
        override fun run() {
            if (mAutoIncrement) {
                increment()
                repeatUpdateHandler.postDelayed(RptUpdater(), REP_DELAY)
            } else if (mAutoDecrement) {
                decrement()
                repeatUpdateHandler.postDelayed(RptUpdater(), REP_DELAY)
            }
        }
    }

    fun decrement() {
        if (croller.progress > croller.min) croller.progress--
    }

    fun increment() {
        if (croller.progress < croller.max) croller.progress++
    }

    override fun getTitle(): String {
        return "DetailTvFrag"
    }
}