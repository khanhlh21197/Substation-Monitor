package com.khanhlh.substationmonitor.ui.main.fragments.room

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.FragmentRoomBinding
import com.khanhlh.substationmonitor.extensions.*
import com.khanhlh.substationmonitor.helper.recyclerview.ItemClickPresenter
import com.khanhlh.substationmonitor.helper.recyclerview.SingleTypeAdapter
import com.khanhlh.substationmonitor.model.Nha
import com.khanhlh.substationmonitor.model.Phong
import com.khanhlh.substationmonitor.model.PhongResponse
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import kotlinx.android.synthetic.main.fragment_home.*


class RoomFragment : BaseFragment<FragmentRoomBinding, RoomViewModel>(),
    ItemClickPresenter<Phong> {
    private lateinit var mqttHelper: MqttHelper
    private lateinit var gson: Gson
    private lateinit var macAddress: String
    private lateinit var phong: Phong
    private val phongs = ObservableArrayList<Phong>()
    private lateinit var idPhong: String

    override fun onFabClick() = toast(getTitle())

    companion object {
        const val ID_ROOM = "ID_ROOM"
    }

    var idNha = ""
    private val RESULT_LOAD_IMAGE = 1
    lateinit var imageView: ImageView
    lateinit var txtInputDevice: EditText

    private val mAdapter by lazy {
        SingleTypeAdapter<Phong>(mContext, R.layout.item_room, phongs).apply {
            itemPresenter = this@RoomFragment
        }
    }

    override fun initView() {
        vm = RoomViewModel(MyApp())
        mBinding.viewModel = vm

        initRecycler()
        fab.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                addRoom()
            }
        }
    }

    private fun initMqtt() {
        macAddress = getMacAddr()!!

        gson = Gson()
        mqttHelper = MqttHelper(requireActivity())

        macAddress.let {
            mqttHelper.connect(it, messageCallBack = object : MqttHelper.MessageCallBack {
                override fun onSuccess(message: String) {
                    val it = fromJson<PhongResponse>(message)
                    if ("0" == it.errorCode && "true" == it.result) {
                        if (!it.message.isNullOrEmpty()) {
                            idPhong = it.message.toString()
                            phong.idphong = idPhong
                            phongs.add(phong)
                        } else {
                            val rooms: ArrayList<Phong> = it.id!!
                            if (phongs.isEmpty()) {
                                phongs.addAll(rooms)
                            }
                        }
                    } else {
                        toast("Error")
                    }
                }

                override fun onError(error: Throwable) {
                    toast(error.toString())
                }
            })
        }
    }

    @SuppressLint("CheckResult")
    private fun getBundleData() {
        if (arguments != null) {
            idNha = requireArguments().getString("idnha")!!
            val nha = Nha(idnha = idNha, mac = getMacAddr()!!)

            mqttHelper.isConnected.observe(this, Observer<Boolean> {
                if (it) {
                    vm.hideLoading()
                    publishMessage("loginphong", toJson(nha)!!)
                } else {
                    vm.showLoading()
                }
            })
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

    override fun getLayoutId(): Int = R.layout.fragment_room
    override fun onItemClick(v: View?, item: Phong) {
        logD(item.idphong)
        var sendId = ""
        if (item.idphong.isNotEmpty()) {
            sendId = item.idphong
        } else {
            sendId = item._id
        }
        val bundle = bundleOf("idphong" to sendId)
        navigate(R.id.deviceFragment, bundle)
//        when ((item.type)) {
//            DeviceType.AC -> navigate(R.id.detailAcFragment, bundle)
//            DeviceType.FAN -> navigate(R.id.detailDeviceFragment, bundle)
//            DeviceType.LIGHT -> navigate(R.id.detailLightFragment, bundle)
//            DeviceType.TEMP -> navigate(R.id.detailDeviceFragment, bundle)
//            DeviceType.TV -> navigate(R.id.detailTvFrag, bundle)
//        }
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

    override fun onImageClick(v: View?) {
        imageView = v as ImageView
        val i = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(i, RESULT_LOAD_IMAGE)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun addRoom() {
        val inflater = layoutInflater
        val alertLayout: View =
            inflater.inflate(R.layout.dialog_add_device, null)
        txtInputDevice = alertLayout.findViewById(R.id.edtName)
        val txtLabel = alertLayout.findViewById(R.id.txtLabel) as TextView
        val scanBarcode =
            alertLayout.findViewById<ImageView>(R.id.scanName)
        val alert =
            AlertDialog.Builder(mContext)
        alert.setTitle(R.string.app_name)
        txtLabel.text = getString(R.string.add_room)
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
            if (txtInputDevice.text != null) {
                phong = Phong("", getMacAddr()!!, txtInputDevice.text.toString(), idNha)
                publishMessage("registerphong", toJson(phong)!!)
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
                    txtInputDevice.setText(result.contents)
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

    override fun onItemLongClick(v: View?, item: Phong): Boolean {
        return true
    }

    override fun onDeleteClick(v: View?, item: Phong) {

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

    override fun onSwitchChange(isChecked: Boolean, item: Phong) {
        logD("onSwitchChange: $isChecked")
    }

    override fun getTitle(): String {
        return "RoomFragment"
    }

}
