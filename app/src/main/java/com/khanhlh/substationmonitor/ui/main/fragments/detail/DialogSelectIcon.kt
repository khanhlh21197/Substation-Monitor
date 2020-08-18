package com.khanhlh.substationmonitor.ui.main.fragments.detail

import android.graphics.Point
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseDialogFragment
import com.khanhlh.substationmonitor.databinding.DialogSelectIconBinding
import com.khanhlh.substationmonitor.extensions.fromJson
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.helper.recyclerview.BindingViewHolder
import com.khanhlh.substationmonitor.helper.recyclerview.ItemClickPresenter
import com.khanhlh.substationmonitor.helper.recyclerview.ItemDecorator
import com.khanhlh.substationmonitor.helper.recyclerview.SingleTypeAdapter
import com.khanhlh.substationmonitor.helper.widget.ColorBrewer
import com.khanhlh.substationmonitor.model.Icon
import com.khanhlh.substationmonitor.model.ThietBiResponse
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import com.khanhlh.substationmonitor.ui.main.fragments.device.DeviceViewModel
import kotlinx.android.synthetic.main.detail_light_frag.*
import kotlinx.android.synthetic.main.dialog_select_icon.*
import java.util.*

class DialogSelectIcon : BaseDialogFragment<DialogSelectIconBinding, DeviceViewModel>(),
    View.OnClickListener, ItemClickPresenter<Icon> {
    private var wheelScrolled = false
    private lateinit var mqttHelper: MqttHelper
    private lateinit var gson: Gson
    private lateinit var idthietbi: String
    private lateinit var iduser: String
    private var countDown = false
    private var icons = ObservableArrayList<Icon>()

    companion object {
        const val ID_DEVICE = "ID_DEVICE"
        const val TAG = "DetailDeviceFragment"
        const val REP_DELAY = 1L
    }

    private val colorArray by lazy { ColorBrewer.Pastel2.getColorPalette(20) }
    val iconAdapter by lazy {
        SingleTypeAdapter<Icon>(mContext, R.layout.item_icon, icons).apply {
            itemPresenter = this@DialogSelectIcon
            itemDecorator = object : ItemDecorator {
                override fun decorator(
                    holder: BindingViewHolder<ViewDataBinding>?,
                    position: Int,
                    viewType: Int
                ) {
                    if (position > 0) {
                        holder?.binding?.root?.background?.setTint(colorArray[position])
                    }
                }
            }
        }
    }

    fun newInstance(idthietbi: String, iduser: String): DialogSelectIcon {
        val args = Bundle()

        args.putString("idthietbi", idthietbi)
        args.putString("iduser", iduser)

        val fragment = DialogSelectIcon()
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var icon = Icon(R.drawable.ic_add_black_24dp)
        icons.add(icon)
        icon = Icon(R.drawable.ic_launcher_background)
        icons.add(icon)
        icon = Icon(R.drawable.ic_action_name)
        icons.add(icon)
        icon = Icon(R.drawable.ic_air_conditioner)
        icons.add(icon)
        icon = Icon(R.drawable.ic_address)
        icons.add(icon)
        icon = Icon(R.drawable.ic_arrow_back_black_24dp)
        icons.add(icon)
        icon = Icon(R.drawable.ic_arrow_down)
        icons.add(icon)
        icon = Icon(R.drawable.ic_arrow_up)
        icons.add(icon)
        icon = Icon(R.drawable.ic_barcode)
        icons.add(icon)
        icon = Icon(R.drawable.ic_bath_room)
        icons.add(icon)
        icon = Icon(R.drawable.ic_bulb_off)
        icons.add(icon)
        icon = Icon(R.drawable.ic_bulb_on)
        icons.add(icon)
        icon = Icon(R.drawable.ic_light_bulb_off)
        icons.add(icon)
    }

    override fun initView() {
        vm = DeviceViewModel()

        initListener()
        initAdapter()
    }

    private fun initAdapter() {
        recycler.apply {
            layoutManager = GridLayoutManager(mContext, 5)
            adapter = iconAdapter
        }
    }

    override fun onResume() {
        super.onResume()

        val window = dialog!!.window
        val size = Point()

        val display: Display = window!!.windowManager.defaultDisplay
        display.getSize(size)

        val width: Int = size.x
        val height: Int = size.y

        window.setLayout((width * 0.5).toInt(), (height * 0.5).toInt())
        window.setGravity(Gravity.CENTER)

        initMqtt()
    }

    private fun initMqtt() {
        mqttHelper = MqttHelper(requireActivity())
    }

    private fun initListener() {
    }

    override fun getLayoutId(): Int = R.layout.dialog_select_icon


    override fun onClick(v: View?) {
        when (v!!.id) {

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

    override fun onPause() {
        super.onPause()
        mqttHelper.close()
    }

    override fun onItemClick(v: View?, item: Icon) {

    }

    override fun onImageClick(v: View?) {

    }

    override fun onDeleteClick(v: View?, item: Icon) {

    }

    override fun onSwitchChange(isChecked: Boolean, item: Icon) {

    }
}