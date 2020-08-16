package com.khanhlh.substationmonitor.ui.main.fragments.home

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
import com.khanhlh.substationmonitor.databinding.FragmentHomeBinding
import com.khanhlh.substationmonitor.extensions.*
import com.khanhlh.substationmonitor.helper.recyclerview.ItemClickPresenter
import com.khanhlh.substationmonitor.helper.recyclerview.SingleTypeAdapter
import com.khanhlh.substationmonitor.helper.shared_preference.get
import com.khanhlh.substationmonitor.model.Nha
import com.khanhlh.substationmonitor.model.NhaResponse
import com.khanhlh.substationmonitor.model.UserTest
import com.khanhlh.substationmonitor.mqtt.MqttHelper
import com.khanhlh.substationmonitor.ui.login.LoginActivity
import com.khanhlh.substationmonitor.utils.USER_PREF
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(),
    ItemClickPresenter<Nha> {
    private val RESULT_LOAD_IMAGE = 1
    private lateinit var imageView: ImageView
    private lateinit var txtInputDevice: EditText
    private lateinit var txtLabel: TextView
    private lateinit var id: String
    private lateinit var mqttHelper: MqttHelper
    private lateinit var gson: Gson
    private lateinit var macAddress: String
    private lateinit var idNha: String
    private lateinit var idArray: Array<String>
    lateinit var nha: Nha
    val homes = ObservableArrayList<Nha>()
    private var publishInfo = ""
    private lateinit var response: NhaResponse
    private lateinit var userJson: String
    private lateinit var sharedPref: SharedPreferences

    override val onFabClick: View.OnClickListener
        get() = View.OnClickListener { toast(getTitle()) }

    companion object {
        const val REGISTER_NHA = "registernha"
        const val LOGIN_USER = "loginuser"
    }

    private val mAdapter by lazy {
        SingleTypeAdapter<Nha>(mContext, R.layout.item_home, homes).apply {
            itemPresenter = this@HomeFragment
        }
    }

    override fun initView() {
        vm = HomeViewModel(MyApp())
        mBinding.viewModel = vm

        initRecycler()
        fab.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                addHome()
            }
        }
        initMqtt()
        getBundleData()
    }

    private fun initMqtt() {
        macAddress = getMacAddr()!!

        gson = Gson()
        mqttHelper = MqttHelper(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        connectMqtt()
    }

    private fun connectMqtt() {
        macAddress.let {
            mqttHelper.connect(it, messageCallBack = object : MqttHelper.MessageCallBack {
                override fun onSuccess(message: String) {
                    val baseResponse: NhaResponse = fromJson(message)
                    if ("0" == baseResponse.errorCode && "true" == baseResponse.result) {
                        when (publishInfo) {
                            REGISTER_NHA -> {
                                if (!baseResponse.message.isNullOrEmpty()) {
                                    idNha = baseResponse.message.toString()
                                    nha.idnha = idNha
                                    homes.add(nha)
                                }
                            }
                            LOGIN_USER -> {
                                val nhas: ArrayList<Nha> = baseResponse.id!!
                                if (homes.isEmpty()) {
                                    homes.addAll(nhas)
                                }
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

    private fun getBundleData() {
        sharedPref = requireActivity().getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
        val email = sharedPref.get(LoginActivity.USER_EMAIL, LoginActivity.DEFAULT_EMAIL)
        val pass = sharedPref.get(LoginActivity.USER_PASSWORD, LoginActivity.DEFAULT_PASSWORD)
        val user = UserTest(email, pass, mac = getMacAddr()!!)
        val args = arguments
        if (args?.getSerializable("nha") != null) {
            response = args.getSerializable("nha") as NhaResponse
            userJson = args.getString("user") as String
            id = response.message!!
            val nhas: ArrayList<Nha> = response.id!!
            if (homes.isEmpty()) {
                homes.addAll(nhas)
            }
        } else {
            publishMessage(LOGIN_USER, toJson(user)!!)
        }
//        idArray = response.id!!.split(",").toTypedArray()
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
//            isPrepared = true
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_home
    override fun onItemClick(v: View?, item: Nha) {
        logD(item.tennha)
        val bundle = bundleOf("idnha" to item._id)
        navigate(R.id.roomFragment, bundle)
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
    private fun addHome() {
        val inflater = layoutInflater
        val alertLayout: View =
            inflater.inflate(R.layout.dialog_add_device, null)
        txtInputDevice = alertLayout.findViewById(R.id.deviceName)
        txtLabel = alertLayout.findViewById(R.id.txtLabel)
        val scanBarcode =
            alertLayout.findViewById<ImageView>(R.id.scanBarcode)
        val alert =
            AlertDialog.Builder(mContext)
        alert.setTitle(R.string.app_name)
        txtLabel.text = (getString(R.string.add_home))
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
                registerHome(txtInputDevice.text.toString())
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

    private fun registerHome(ten: String) {
        nha = Nha("", id, ten, getMacAddr()!!)
        publishMessage(REGISTER_NHA, toJson(nha)!!)
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

    override fun onItemLongClick(v: View?, item: Nha) : Boolean {
        return false
    }

    override fun onDeleteClick(v: View?, item: Nha) {
        logD(item.tennha)
    }

    override fun onPause() {
        super.onPause()
        mqttHelper.close()
    }

    override fun onSwitchChange(isChecked: Boolean, item: Nha) {
        logD("onSwitchChange: $isChecked")
    }

    private fun publishMessage(topic: String, json: String) {
        publishInfo = topic
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

    override fun getTitle(): String {
        return "HomeFragment"
    }

}
