package com.khanhlh.substationmonitor.ui.main.fragments.device

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.FragmentDeviceBinding
import com.khanhlh.substationmonitor.extensions.*
import com.khanhlh.substationmonitor.helper.recyclerview.ItemClickPresenter
import com.khanhlh.substationmonitor.helper.recyclerview.SingleTypeAdapter
import com.khanhlh.substationmonitor.model.Lenh
import com.khanhlh.substationmonitor.model.ThietBi
import com.khanhlh.substationmonitor.model.ThietBiResponse
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import kotlinx.android.synthetic.main.fragment_device.*
import kotlinx.android.synthetic.main.fragment_home.fab
import kotlinx.android.synthetic.main.fragment_home.recycler
import java.util.*

class DeviceFragment : BaseFragment<FragmentDeviceBinding, DeviceViewModel>(),
    ItemClickPresenter<ThietBi> {
    private lateinit var mqttHelper: MqttHelper
    private lateinit var gson: Gson
    private lateinit var macAddress: String
    private lateinit var thietbi: ThietBi
    private val thietbis = ObservableArrayList<ThietBi>()
    private lateinit var idThietBi: String
    private var currentIndex: Int = 0
    private var publishInfo: String = ""
    private lateinit var deviceName: EditText
    private lateinit var tbTest: ThietBi

    companion object {
        const val ID_ROOM = "ID_ROOM"
        const val LOGIN_DEVICE = "loginthietbi"
        const val STATUS_PHONG = "StatusPHONG"
        const val REGISTER_DEVICE = "registerthietbi"
        const val DELETE_DEVICE = "deletethietbi"
        const val DELETE_ALL_DEVICE = "deleteallthietbi"
        const val UPDATE_DEVICE = "updatethietbi"
    }

    var idPhong = ""
    private val RESULT_LOAD_IMAGE = 1
    lateinit var imageView: ImageView

    private val mAdapter by lazy {
        SingleTypeAdapter<ThietBi>(mContext, R.layout.item_device, thietbis).apply {
            itemPresenter = this@DeviceFragment
        }
    }

    override fun initView() {
        vm = DeviceViewModel(MyApp())
        mBinding.viewModel = vm

        initRecycler()
        fab.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                addDevice()
            }
        }

        fabDeleteAll.setOnClickListener {
            deleteAllDevices()
        }
    }

    @SuppressLint("CheckResult")
    private fun deleteAllDevices() {
        createDialog(
            requireActivity(),
            getString(R.string.delete_device),
            getString(R.string.app_name),
            getString(R.string.cancel),
            getString(R.string.ok),
            null,
            View.OnClickListener {
                val tb = ThietBi(idphong = idPhong, mac = getMacAddr()!!)
                publishMessage(DELETE_ALL_DEVICE, toJson(tb)!!)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        initMqtt()
        getBundleData()
    }

    override fun onPause() {
        super.onPause()
        mqttHelper.close()
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
                        when (publishInfo) {
                            REGISTER_DEVICE -> {
                                thietbi.id = response.message!!
                                thietbis.add(thietbi)
                            }
                            STATUS_PHONG -> {
                                val devices: ArrayList<ThietBi> = response.id!!
                                thietbis.clear()
                                tbTest = ThietBi(
                                    tenthietbi = "TenTest",
                                    mathietbi = "MaTest",
                                    status = "bat"
                                )
                                thietbis.add(tbTest)
                                thietbis.addAll(devices)
                            }
                            LOGIN_DEVICE -> {
                                val devices: ArrayList<ThietBi> = response.id!!
                                thietbis.clear()
                                tbTest = ThietBi(
                                    tenthietbi = "TenTest",
                                    mathietbi = "MaTest",
                                    status = "bat"
                                )
                                thietbis.add(tbTest)
                                thietbis.addAll(devices)
                            }
                            DELETE_DEVICE -> {
                                thietbis.removeAt(currentIndex)
                            }
                            DELETE_ALL_DEVICE -> {
                                thietbis.clear()
                            }
                        }
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

    private fun getBundleData() {
        if (arguments != null) {
            idPhong = requireArguments().getString("idphong")!!
            val tb = ThietBi(idphong = idPhong, mac = getMacAddr()!!)

//            publishMessage(LOGIN_DEVICE, toJson(tb)!!)
            publishMessage(STATUS_PHONG, toJson(tb)!!)
        }
    }

    private fun initRecycler() {
        recycler.apply {
            layoutManager = GridLayoutManager(mContext, 2)
            adapter = mAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
//                    outRect.top = activity?.dpToPx(R.dimen.xdp_12_0) ?: 0
                }
            })
            isPrepared = true
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_device
    override fun onItemClick(v: View?, item: ThietBi) {
        when (v!!.id) {
            R.id.switchBtn -> {
                logD(item.id)
            }
            R.id.card_view -> {
                logD(item.id)
                val bundle = bundleOf("idthietbi" to item.mathietbi, "idphong" to idPhong)
                navigate(R.id.detailLightFragment, bundle)
            }
        }
//        when ((item.type)) {
//            DeviceType.AC -> navigate(R.id.detailAcFragment, bundle)
//            DeviceType.FAN -> navigate(R.id.detailDeviceFragment, bundle)
//            DeviceType.LIGHT -> navigate(R.id.detailLightFragment, bundle)
//            DeviceType.TEMP -> navigate(R.id.detailDeviceFragment, bundle)
//            DeviceType.TV -> navigate(R.id.detailTvFrag, bundle)
//        }
    }

    override fun onImageClick(v: View?) {
        imageView = v as ImageView
        val i = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(i, RESULT_LOAD_IMAGE)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun addDevice() {
        val inflater = layoutInflater
        val alertLayout: View =
            inflater.inflate(R.layout.dialog_add_device, null)
        deviceName = alertLayout.findViewById<EditText>(R.id.deviceName)
        val deviceId = alertLayout.findViewById<EditText>(R.id.deviceId)
        val tipDeviceId = alertLayout.findViewById<TextInputLayout>(R.id.tipDeviceId)

        tipDeviceId.visibility = View.VISIBLE

        val scanBarcode =
            alertLayout.findViewById<ImageView>(R.id.scanBarcode)
        val alert =
            AlertDialog.Builder(mContext)
        alert.setTitle(R.string.app_name)
        alert.setView(alertLayout)
        alert.setCancelable(false)
        scanBarcode.setOnClickListener { v: View? ->
            val scanIntent = IntentIntegrator(activity)
                .setBeepEnabled(false)
                .createScanIntent()
            startActivityForResult(scanIntent, 1)
        }
        alert.setNegativeButton(
            getString(R.string.cancel)
        ) { dialog: DialogInterface?, which: Int ->
            Toast.makeText(
                activity,
                "Cancel clicked",
                Toast.LENGTH_SHORT
            ).show()
        }
        alert.setPositiveButton(
            getString(R.string.ok)
        ) { dialog: DialogInterface?, which: Int ->
            if (deviceName.text != null) {
                thietbi = ThietBi(
                    "",
                    getMacAddr()!!,
                    idPhong,
                    deviceName.text.toString().toUpperCase(Locale.ROOT),
                    deviceId.text.toString().toUpperCase(Locale.ROOT)
                )
                publishMessage(REGISTER_DEVICE, toJson(thietbi)!!)
            } else {
                Toast.makeText(
                    activity,
                    getString(R.string.input_device_name),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val dialog = alert.create()
        dialog.show()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(49374, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                if (requestCode == 1) {
                    deviceName.setText(result.contents)
                }
            }
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            val selectedImage: Uri? = data.data
            val filePathColumn =
                arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor = mContext.contentResolver.query(
                selectedImage!!,
                filePathColumn, null, null, null
            )!!
            cursor.moveToFirst()
            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
            val picturePath: String = cursor.getString(columnIndex)
            cursor.close()
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }

    override fun onItemLongClick(v: View?, item: ThietBi) {
        createDialog(
            requireActivity(),
            getString(R.string.app_name),
            getString(R.string.app_name),
            getString(R.string.cancel),
            getString(R.string.ok),
            null,
            null
        )
    }

    override fun onDeleteClick(v: View?, item: ThietBi) {
        currentIndex = thietbis.indexOf(item)
        createDialog(
            requireActivity(),
            getString(R.string.delete_device),
            getString(R.string.app_name),
            getString(R.string.cancel),
            getString(R.string.ok),
            null,
            View.OnClickListener {
                val tb = ThietBi(mac = getMacAddr()!!, mathietbi = item.mathietbi)
                publishMessage(DELETE_DEVICE, toJson(tb)!!)
            }
        )
    }

    private fun publishMessage(topic: String, json: String) {
        this.publishInfo = topic
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

    override fun onSwitchChange(isChecked: Boolean, item: ThietBi) {
        logD("${item.tenthietbi}}: $isChecked")
        val lenh = Lenh()
        lenh.idphong = idPhong
        if (isChecked) {
            lenh.lenh = "bat"
        } else {
            lenh.lenh = "tat"
        }
        val idthietbi = item.mathietbi.toUpperCase(Locale.ROOT)
        publishMessage("P$idthietbi", toJson(lenh)!!)
    }

}
