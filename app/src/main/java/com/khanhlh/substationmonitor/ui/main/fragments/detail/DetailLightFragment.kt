package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.app.Dialog
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.Window
import android.widget.TextView
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.DetailLightFragBinding
import com.yanzhenjie.wheel.OnWheelChangedListener
import com.yanzhenjie.wheel.OnWheelScrollListener
import com.yanzhenjie.wheel.WheelView
import com.yanzhenjie.wheel.adapters.ArrayWheelAdapter
import kotlinx.android.synthetic.main.detail_light_frag.*


class DetailLightFragment : BaseFragment<DetailLightFragBinding, DetailDeviceViewModel>(),
    View.OnClickListener {
    private var wheelScrolled = false
    lateinit var wheelMenu1: Array<Int>
    lateinit var wheelMenu2: Array<Int>

    companion object {
        const val ID_DEVICE = "ID_DEVICE"
        const val TAG = "DetailDeviceFragment"
        const val REP_DELAY = 1L
    }

    override fun initView() {
        vm = DetailDeviceViewModel()
        mBinding.vm = vm
        getBundleData()
        initWheelAdapter()
        initListener()
        onSwitchChange()
    }

    private fun getBundleData() {
        val args = arguments
        val idDevice = args!!.getString(DetailTvFrag.ID_DEVICE)
        vm.observerDevice(idDevice as String)
    }

    private fun initListener() {
        timerOn.setOnClickListener(this)
        timerOff.setOnClickListener(this)
    }

    private fun initWheelAdapter() {
        wheelMenu1 = arrayOf(1, 2, 3, 4, 5)
        wheelMenu2 = arrayOf(2, 3, 4, 1, 21, 2)
    }

    private fun onSwitchChange() {
        lightSwitch.setOnCheckedChangeListener { _, isChecked ->
            val drawable: TransitionDrawable = light.drawable as TransitionDrawable
            if (isChecked) {
                drawable.startTransition(100)
            } else {
                drawable.reverseTransition(100)
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.detail_light_frag


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.timerOn, R.id.timerOff -> showDialog(getString(R.string.app_name))
        }
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
//                updateStatus()
            }
        }

    private fun showDialog(title: String) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.timer_picker_wheel)

        val yesBtn = dialog.findViewById(R.id.ok) as TextView
        val noBtn = dialog.findViewById(R.id.cancel) as TextView
        val hourWheel = dialog.findViewById(R.id.hourWheel) as WheelView
        val minuteWheel = dialog.findViewById(R.id.minuteWheel) as WheelView

        minuteWheel.setAdapter(ArrayWheelAdapter<Int>(activity, wheelMenu1))
        minuteWheel.visibleItems = 2
        minuteWheel.currentItem = 0
        minuteWheel.addChangingListener(changedListener)
        minuteWheel.addScrollingListener(scrolledListener)

        hourWheel.setAdapter(ArrayWheelAdapter<Int>(activity, wheelMenu2))
        hourWheel.visibleItems = 2
        hourWheel.currentItem = 0
        hourWheel.addChangingListener(changedListener)
        hourWheel.addScrollingListener(scrolledListener)

        yesBtn.setOnClickListener {
            dialog.dismiss()
        }

        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}