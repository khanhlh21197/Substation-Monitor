package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseDialogFragment
import com.khanhlh.substationmonitor.databinding.DetailLightFragBinding
import com.khanhlh.substationmonitor.extensions.fromJson
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.extensions.toJson
import com.khanhlh.substationmonitor.model.Lenh
import com.khanhlh.substationmonitor.model.ThietBiResponse
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import com.yanzhenjie.wheel.OnWheelChangedListener
import com.yanzhenjie.wheel.OnWheelScrollListener
import com.yanzhenjie.wheel.WheelView
import kotlinx.android.synthetic.main.detail_light_frag.*
import java.util.*

class DetailLightFragment : BaseDialogFragment<DetailLightFragBinding, DetailDeviceViewModel>(),
    View.OnClickListener {
    private var wheelScrolled = false
    private lateinit var mqttHelper: MqttHelper
    private lateinit var gson: Gson
    private lateinit var idthietbi: String
    private lateinit var iduser: String
    private var countDown = false

    companion object {
        const val ID_DEVICE = "ID_DEVICE"
        const val TAG = "DetailDeviceFragment"
        const val REP_DELAY = 1L
    }

    fun newInstance(idthietbi: String, iduser: String): DetailLightFragment {
        val args = Bundle()

        args.putString("idthietbi", idthietbi)
        args.putString("iduser", iduser)

        val fragment = DetailLightFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        vm = DetailDeviceViewModel(MyApp())
        mBinding.vm = vm

        radioGroupListener()
        initListener()
        onSwitchChange()
        setupSeekBar()
    }

    private fun radioGroupListener() {
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioTimer -> {
                    countDown = false
                    llTimerOff.visibility = View.GONE
                }
                R.id.radioCountDown -> {
                    countDown = true
                    llTimerOff.visibility = View.VISIBLE
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        initMqtt()
        getBundleData()
    }

    private fun initMqtt() {
        mqttHelper = MqttHelper(requireActivity())
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
        args!!.let {
            idthietbi = it.getString("idthietbi").toString().toUpperCase(Locale.getDefault())
            iduser = it.getString("iduser").toString()
        }
        idthietbi.let {
            mqttHelper.connect("S$it", messageCallBack = object : MqttHelper.MessageCallBack {
                override fun onSuccess(message: String) {
                    val tb = fromJson<ThietBiResponse>(message)
                    if ("0" == tb.errorCode) {
                        if ("true" == tb.result) {
                            lightSwitch.isChecked = "bat" == tb.message
                        } else {
                            toast("Gui lenh that bai")
                        }
                    } else {
                        toast("Vui long thu lai sau")
                    }
                }

                override fun onError(error: Throwable) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    private fun initListener() {
        timerOn.setOnClickListener(this)
        timerOff.setOnClickListener(this)
    }

    private fun onSwitchChange() {
        lightSwitch.setOnCheckedChangeListener { _, isChecked ->
            val drawable: TransitionDrawable = light.drawable as TransitionDrawable
            val lenh = Lenh()
            lenh.iduser = iduser
            if (isChecked) {
                lenh.lenh = "bat"
                drawable.startTransition(100)
            } else {
                lenh.lenh = "tat"
                drawable.reverseTransition(100)
            }
            publishMessage("P$idthietbi", toJson(lenh)!!)
        }
        lightSwitch.isEnabled = false
        Handler().postDelayed({
            if (lightSwitch != null) {
                lightSwitch.isEnabled = true
            }
        }, 1000)
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
        OnWheelChangedListener { _, _, newValue ->
            if (!wheelScrolled) {
                updateStatus(newValue)
            }
        }

    private fun updateStatus(newValue: Int) {
        tvTimerOff.text = newValue.toString()
        tvTimerOn.text = newValue.toString()
    }

    @SuppressLint("CheckResult")
    private fun diffTime(
        currentHour: Int,
        currentMinute: Int,
        hour: Int,
        minute: Int,
        timerOn: Boolean,
        countDownTime: Int
    ) {
        var gioBat = ""
        var phutBat = ""
        var gioTat = ""
        var phutTat = ""
        val lenh = Lenh()

        val diffHour = hour - currentHour
        var diffMinute = 0
        if (minute > currentMinute) {
            diffMinute = minute - currentMinute
        } else {
            diffMinute = 60 + minute - currentMinute
        }
        if (timerOn) {
            gioBat = hour.toString()
            if (gioBat.toInt() < 10) gioBat = "0$gioBat"
            phutBat = minute.toString()
            if (phutBat.toInt() < 10) phutBat = "0$phutBat"
            lenh.lenh = "hengiobat"

            if (countDown) {
                lenh.param = "$gioBat&$phutBat&$countDownTime"
                toast("Bật đèn trong $countDownTime giây")
            } else {
                lenh.param = "$gioBat&$phutBat"
                toast("Bật đèn sau $diffHour giờ và $diffMinute phút")
            }
        } else {
            gioTat = hour.toString()
            if (gioTat.toInt() < 10) gioTat = "0$gioTat"
            phutTat = minute.toString()
            if (phutTat.toInt() < 10) phutTat = "0$phutBat"
            lenh.lenh = "hengiotat"
            lenh.param = "$gioTat&$phutTat"
            toast("Tắt đèn sau $diffHour giờ và $diffMinute phút")
        }
        val message: String = "{" +
                "\"lenh\":\"${lenh.lenh}\"," +
                "\"param\":\"${lenh.param}\"" +
                "}"
        publishMessage("P$idthietbi", message)
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
        val llTurnOnIn = dialog.findViewById<LinearLayout>(R.id.llTurnOnIn)
        val edtOnIn = dialog.findViewById<EditText>(R.id.edtOnIn)
        var countDownTime: Int = 0
        if (countDown) llTurnOnIn.visibility = View.VISIBLE

        timePicker.setIs24HourView(true)
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            pickedHour = hourOfDay
            pickedMinute = minute
        }

        val yesBtn = dialog.findViewById(R.id.ok) as TextView
        val noBtn = dialog.findViewById(R.id.cancel) as TextView

        yesBtn.setOnClickListener {
            if (countDown) {
                countDownTime = edtOnIn.text.toString().toInt()
            }
            diffTime(mHour, mMinute, pickedHour, pickedMinute, timerOn, countDownTime)
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

    override fun onPause() {
        super.onPause()
        mqttHelper.close()
    }
}