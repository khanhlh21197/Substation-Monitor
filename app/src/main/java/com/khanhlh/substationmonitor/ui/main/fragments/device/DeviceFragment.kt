package com.khanhlh.substationmonitor.ui.main.fragments.device

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.ObservableArrayList
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.khanhlh.substationmonitor.MyApp
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.FragmentDeviceBinding
import com.khanhlh.substationmonitor.enums.AddType
import com.khanhlh.substationmonitor.extensions.*
import com.khanhlh.substationmonitor.helper.recyclerview.BindingViewHolder
import com.khanhlh.substationmonitor.helper.recyclerview.ItemClickPresenter
import com.khanhlh.substationmonitor.helper.recyclerview.ItemDecorator
import com.khanhlh.substationmonitor.helper.recyclerview.SingleTypeAdapter
import com.khanhlh.substationmonitor.helper.shared_preference.get
import com.khanhlh.substationmonitor.helper.shared_preference.put
import com.khanhlh.substationmonitor.helper.widget.ColorBrewer
import com.khanhlh.substationmonitor.model.*
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import com.khanhlh.substationmonitor.ui.login.LoginActivity
import com.khanhlh.substationmonitor.ui.main.MainViewModel
import com.khanhlh.substationmonitor.ui.main.fragments.detail.DetailLightFragment
import com.khanhlh.substationmonitor.ui.main.fragments.detail.DialogSelectIcon
import com.khanhlh.substationmonitor.utils.USER_PREF
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_device.*
import java.util.*

class DeviceFragment : BaseFragment<FragmentDeviceBinding, DeviceViewModel>(),
    ItemClickPresenter<ThietBi> {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var mqttHelper: MqttHelper
    private lateinit var gson: Gson
    private lateinit var macAddress: String
    private lateinit var thietbi: ThietBi
    private val thietbis = ObservableArrayList<ThietBi>()
    private val phongs = ObservableArrayList<Phong>()
    private lateinit var idThietBi: String
    private var currentIndex: Int = 0
    private var publishInfo: String = ""
    private lateinit var name: EditText
    private var listTB = arrayListOf<ThietBi>()
    private var iduser = ""
    private lateinit var sharedPref: SharedPreferences
    private lateinit var addType: AddType
    private val colorArray by lazy { ColorBrewer.Pastel2.getColorPalette(20) }

    companion object {
        const val ID_ROOM = "ID_ROOM"
        const val LOGIN_DEVICE = "loginthietbi"
        const val STATUS_PHONG = "StatusPHONG"
        const val REGISTER_DEVICE = "registerthietbi"
        const val DELETE_DEVICE = "deletethietbi"
        const val DELETE_ALL_DEVICE = "deleteallthietbi"
        const val UPDATE_DEVICE = "updatethietbi"
    }

    private val RESULT_LOAD_IMAGE = 1
    lateinit var imageView: ImageView

    private val deviceAdapter by lazy {
        SingleTypeAdapter<ThietBi>(mContext, R.layout.item_device, thietbis).apply {
            itemPresenter = this@DeviceFragment
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

    private val groupAdapter by lazy {
        SingleTypeAdapter<Phong>(mContext, R.layout.item_room, phongs).apply {
            itemPresenter = object : ItemClickPresenter<Phong> {
                override fun onItemClick(v: View?, item: Phong) {
                    toast(item.tenphong)
                }

                override fun onImageClick(v: View?) {

                }

                override fun onDeleteClick(v: View?, item: Phong) {

                }

                override fun onSwitchChange(isChecked: Boolean, item: Phong) {

                }
            }
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onFabClick() {
        add()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        vm = DeviceViewModel(MyApp())
        mBinding.viewModel = vm

        initRecycler()
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
                val tb = ThietBi(iduser = iduser, mac = getMacAddr()!!)
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
                                thietbis.addAll(devices)
                            }
                            LOGIN_DEVICE -> {
                                val devices: ArrayList<ThietBi> = response.id!!
                                thietbis.clear()
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
        sharedPref = requireActivity().getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
        val email = sharedPref.get(LoginActivity.USER_EMAIL, LoginActivity.DEFAULT_EMAIL)
        val pass = sharedPref.get(LoginActivity.USER_PASSWORD, LoginActivity.DEFAULT_PASSWORD)
        val user = UserTest(email, pass, mac = getMacAddr()!!)

        if (requireArguments().getSerializable("thietbi") != null) {
            val thietBiResponse = requireArguments().getSerializable("thietbi") as ThietBiResponse
            listTB = thietBiResponse.id!!
            iduser = thietBiResponse.message.toString()
            sharedPref.put("iduser", iduser)

            thietbis.clear()
            thietbis.addAll(listTB)
        } else {
            if (iduser.isNotEmpty()) {
                val tb = ThietBi(iduser = iduser, mac = getMacAddr()!!)
                publishMessage(LOGIN_DEVICE, toJson(tb)!!)
            } else {
                iduser = sharedPref.get("iduser", "")
                val tb = ThietBi(iduser = iduser, mac = getMacAddr()!!)
                publishMessage(LOGIN_DEVICE, toJson(tb)!!)
            }
        }
//            publishMessage(STATUS_PHONG, toJson(tb)!!)
    }

    private fun initRecycler() {
        devicesList.apply {
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = deviceAdapter
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

        groupList.apply {
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = groupAdapter
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
                val lightDetail = DetailLightFragment().newInstance(item.mathietbi, iduser)
                lightDetail.show(requireActivity().supportFragmentManager, "lightDetail")
//                logD(item.id)
//                val bundle = bundleOf("idthietbi" to item.mathietbi, "iduser" to iduser)
//                navigate(R.id.detailLightFragment, bundle)
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
    fun add() {
        val arr = arrayOf(
            getString(R.string.add_room),
            getString(R.string.add_device)
        )

        val inflater = layoutInflater
        val alertLayout: View =
            inflater.inflate(R.layout.dialog_add, null)
        name = alertLayout.findViewById<EditText>(R.id.edtName)
        val txtLabel = alertLayout.findViewById<TextView>(R.id.txtLabel)
        val id = alertLayout.findViewById<EditText>(R.id.edtId)
        val tipId = alertLayout.findViewById<TextInputLayout>(R.id.tipId)
        val spinner = alertLayout.findViewById<Spinner>(R.id.spinner)
        val circleImageView = alertLayout.findViewById<CircleImageView>(R.id.circleImageView)

        circleImageView.setOnClickListener { chooseImage() }

        spinner.visibility = View.VISIBLE
        val spinnerAdapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, arr)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    tipId.visibility = View.GONE
                    addType = AddType.ROOM
                    txtLabel.text = getString(R.string.add_room)
                } else {
                    tipId.visibility = View.VISIBLE
                    addType = AddType.DEVICE
                    txtLabel.text = getString(R.string.add_device)
                }
            }
        }

        spinner.setSelection(0)

        val scanBarcode =
            alertLayout.findViewById<ImageView>(R.id.scanName)
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
                getString(R.string.cancel),
                Toast.LENGTH_SHORT
            ).show()
        }
        alert.setPositiveButton(
            getString(R.string.ok)
        ) { dialog: DialogInterface?, which: Int ->
            when (addType) {
                AddType.ROOM -> {
                    val phong = Phong(tenphong = name.text.toString())
                    phongs.add(phong)
                }
                AddType.DEVICE -> {
                    if (name.text != null) {
                        thietbi = ThietBi(
                            "",
                            getMacAddr()!!,
                            iduser,
                            name.text.toString().toUpperCase(Locale.ROOT),
                            id.text.toString().toUpperCase(Locale.ROOT)
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
            }
        }
        val dialog = alert.create()
        dialog.show()
    }

    private fun chooseImage() {
        val dialogSelectIcon = DialogSelectIcon()
        dialogSelectIcon.show(requireActivity().supportFragmentManager, "DialogSelectIcon")
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
                    name.setText(result.contents)
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

    override fun onItemLongClick(v: View?, item: ThietBi): Boolean {
        createDialog(
            requireActivity(),
            getString(R.string.app_name),
            getString(R.string.app_name),
            getString(R.string.cancel),
            getString(R.string.ok),
            null,
            null
        )
        return true
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
        mqttHelper.isConnected.observe(viewLifecycleOwner, Observer<Boolean> {
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
        lenh.iduser = iduser
        if (isChecked) {
            lenh.lenh = "bat"
            item.trangthai = true
        } else {
            lenh.lenh = "tat"
            item.trangthai = false
        }
        val idthietbi = item.mathietbi.toUpperCase(Locale.ROOT)
        publishMessage("P$idthietbi", toJson(lenh)!!)
    }

    override fun getTitle(): String {
        return "Xin chào"
    }

}
