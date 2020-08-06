package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.Window
import android.widget.SeekBar
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
    private var wheelMenu1 = arrayOfNulls<Int>(24)
    private var wheelMenu2 = arrayOfNulls<Int>(60)

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
        setupWheel()
        setupSeekBar()
    }

    private fun setupSeekBar() {
        seekBar.progress = 125
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                light.colorFilter = setBrightness(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    fun setBrightness(progress: Int): PorterDuffColorFilter? {
        return if (progress >= 100) {
            val value = (progress - 100) * 255 / 100
            PorterDuffColorFilter(Color.argb(value, 255, 255, 255), PorterDuff.Mode.SRC_OVER)
        } else {
            val value = (100 - progress) * 255 / 100
            PorterDuffColorFilter(Color.argb(value, 0, 0, 0), PorterDuff.Mode.SRC_ATOP)
        }
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
        for (i in 1..24) {
            wheelMenu1[i] = i
        }
        for (i in 1..60) {
            wheelMenu2[i] = i
        }
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
                updateStatus(newValue)
            }
        }

    private fun updateStatus(newValue: Int) {
        tvTimerOff.text = newValue.toString()
        tvTimerOn.text = newValue.toString()
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

    private fun setupWheel() {
        hourOnWheel.setAdapter(ArrayWheelAdapter<Int>(activity, wheelMenu1))
        hourOnWheel.visibleItems = 3
        hourOnWheel.currentItem = 0
        hourOnWheel.addChangingListener(changedListener)
        hourOnWheel.addScrollingListener(scrolledListener)

        hourOffWheel.setAdapter(ArrayWheelAdapter<Int>(activity, wheelMenu1))
        hourOffWheel.visibleItems = 3
        hourOffWheel.currentItem = 0
        hourOffWheel.addChangingListener(changedListener)
        hourOffWheel.addScrollingListener(scrolledListener)

        minuteOnWheel.setAdapter(ArrayWheelAdapter<Int>(activity, wheelMenu2))
        minuteOnWheel.visibleItems = 3
        minuteOnWheel.currentItem = 0
        minuteOnWheel.addChangingListener(changedListener)
        minuteOnWheel.addScrollingListener(scrolledListener)

        minuteOffWheel.setAdapter(ArrayWheelAdapter<Int>(activity, wheelMenu2))
        minuteOffWheel.visibleItems = 3
        minuteOffWheel.currentItem = 0
        minuteOffWheel.addChangingListener(changedListener)
        minuteOffWheel.addScrollingListener(scrolledListener)
    }
}