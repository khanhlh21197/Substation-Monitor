package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.core.graphics.drawable.toBitmap
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.DetailAcFragBinding
import com.khanhlh.substationmonitor.extensions.logD
import kotlinx.android.synthetic.main.detail_ac_frag.*


class DetailAcFragment : BaseFragment<DetailAcFragBinding, DetailDeviceViewModel>(),
    View.OnClickListener {
    private var mAutoIncrement = false
    private var mAutoDecrement = false
    val repeatUpdateHandler = Handler()

    companion object {
        const val ID_DEVICE = "ID_DEVICE"
        const val TAG = "DetailDeviceFragment"
        const val REP_DELAY = 1L
    }

    override fun initView() {
        vm = DetailDeviceViewModel(MyApp())
        mBinding.vm = vm
        getBundleData()
        crollerConfigure()
    }

    private fun crollerConfigure() {
        minus.setOnClickListener(this)
        plus.setOnClickListener(this)
        croller.setOnProgressChangedListener {
            croller.label = (it + 17).toString() + " " + 0x00B0.toChar()
            seekBar.progress = it + 17
            logD(seekBar.progress.toString())
        }
        seekBar.max = 15
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                croller.progress = progress
//                thumbAnimation(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    private fun thumbAnimation(progress: Int) {
        val res: Resources = resources
        val thumb: Drawable = res.getDrawable(R.drawable.seekbar_thumb)
        val h: Int = progress / 10
        val bmpOrg = thumb.toBitmap()
        val bmpScaled = Bitmap.createScaledBitmap(bmpOrg, h, h, true)
        val newThumb: Drawable = BitmapDrawable(res, bmpScaled)
        newThumb.setBounds(0, 0, newThumb.intrinsicWidth, newThumb.intrinsicHeight)
        seekBar.thumb = newThumb
    }

    private fun getBundleData() {
        val args = arguments
        val idDevice = args!!.getString(ID_DEVICE)
        vm.observerDevice(idDevice as String)
    }

    override fun getLayoutId(): Int = R.layout.detail_ac_frag
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.minus -> {
                decrement()
            }
            R.id.plus -> {
                increment()
            }
        }
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
}