package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TimePicker
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.DetailLightFragBinding
import com.yanzhenjie.wheel.OnWheelChangedListener
import com.yanzhenjie.wheel.OnWheelScrollListener
import com.yanzhenjie.wheel.WheelView
import kotlinx.android.synthetic.main.detail_light_frag.*
import java.util.*


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
        vm = DetailDeviceViewModel(MyApp())
        mBinding.vm = vm
        getBundleData()
        initListener()
        onSwitchChange()
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
        val idthietbi = args!!.getString("idthietbi")
    }

    private fun initListener() {
        timerOn.setOnClickListener(this)
        timerOff.setOnClickListener(this)
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
            R.id.timerOn -> showDialog(true)
            R.id.timerOff -> showDialog(false)
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

    @SuppressLint("SetTextI18n")
    private fun showTimerPickerDialog(timerOn: Boolean) {
        val c = Calendar.getInstance()
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)

        // Launch Time Picker Dialog

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            activity,
            OnTimeSetListener { view, hourOfDay, minute ->
                diffTime(mHour, mMinute, hourOfDay, minute, timerOn)
                if (timerOn) {
                    tvTimerOn.text = " : $hourOfDay giờ $minute phút"
                } else {
                    tvTimerOff.text = " : $hourOfDay giờ $minute phút"
                }
            },
            mHour,
            mMinute,
            true
        )
        timePickerDialog.show()
    }

    private fun diffTime(
        currentHour: Int,
        currentMinute: Int,
        hour: Int,
        minute: Int,
        timerOn: Boolean
    ) {
        val diffHour = hour - currentHour
        var diffMinute = 0
        if (minute > currentMinute) {
            diffMinute = minute - currentMinute
        } else {
            diffMinute = 60 + minute - currentMinute
        }
        if (timerOn) {
            toast("Bật đèn sau $diffHour giờ và $diffMinute phút")
        } else {
            toast("Tắt đèn sau $diffHour giờ và $diffMinute phút")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDialog(timerOn: Boolean) {
        val c = Calendar.getInstance()
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)
        var pickedHour = 0
        var pickedMinute = 0

        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.timer_picker_wheel)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes = lp

        val timePicker = dialog.findViewById<TimePicker>(R.id.timePicker)
        timePicker.setIs24HourView(true)
        timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            pickedHour = hourOfDay
            pickedMinute = minute
        }

        val yesBtn = dialog.findViewById(R.id.ok) as TextView
        val noBtn = dialog.findViewById(R.id.cancel) as TextView

        yesBtn.setOnClickListener {
            diffTime(mHour, mMinute, pickedHour, pickedMinute, timerOn)
            if (timerOn) {
                tvTimerOn.text = " : $pickedHour giờ $pickedMinute phút"
            } else {
                tvTimerOff.text = " : $pickedHour giờ $pickedMinute phút"
            }
            dialog.dismiss()
        }

        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}