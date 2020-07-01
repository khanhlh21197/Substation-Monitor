package com.khanhlh.substationmonitor.ui.main.fragments.home


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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.base.BaseFragment
import com.khanhlh.substationmonitor.databinding.FragmentHomeBinding
import com.khanhlh.substationmonitor.extensions.logD
import com.khanhlh.substationmonitor.extensions.replaceFragmentSafely
import com.khanhlh.substationmonitor.helper.dialog.alert
import com.khanhlh.substationmonitor.helper.recyclerview.ItemClickPresenter
import com.khanhlh.substationmonitor.helper.recyclerview.SingleTypeAdapter
import com.khanhlh.substationmonitor.model.Device
import com.khanhlh.substationmonitor.ui.main.fragments.detail.DetailDeviceFragment
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : BaseFragment<FragmentHomeBinding>(), ItemClickPresenter<Device> {
    private val RESULT_LOAD_IMAGE = 1
    private lateinit var vm: HomeViewModel
    lateinit var imageView: ImageView
    lateinit var txtInputDevice: EditText

    private val mAdapter by lazy {
        SingleTypeAdapter<Device>(mContext, R.layout.item_device, vm.list).apply {
            itemPresenter = this@HomeFragment
        }
    }

    override fun initView() {
        vm = HomeViewModel()
        mBinding.viewModel = vm

        initRecycler()
        observer()
        fab.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                addDevice()
            }
        }
    }

    private fun observer() {
        vm.observerAllDevices()
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

    override fun getLayoutId(): Int = R.layout.fragment_home
    override fun onItemClick(v: View?, item: Device) {
        logD(item.id)
        (activity as AppCompatActivity).replaceFragmentSafely(
            DetailDeviceFragment.newInstance(item.id),
            "",
            false
        )
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
        txtInputDevice = alertLayout.findViewById<EditText>(R.id.txtInputDevice)
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
            if (txtInputDevice.text != null) {
                vm.updateDevice(txtInputDevice.text.toString(), UpdateDeviceType.ADD).subscribe({
                    toast(it)
                }, { toast(it.toString()) })
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

    override fun onItemLongClick(v: View?, item: Device) {
    }

    override fun onDeleteClick(v: View?, item: Device) {
        alert(
            this,
            R.string.delete_device,
            R.string.app_name,
            R.string.cancel,
            R.string.ok,
            null,
            View.OnClickListener {
                vm.updateDevice(item.id, UpdateDeviceType.REMOVE)
                    .subscribe({ toast(R.string.delete_device_success) },
                        { toast(R.string.delete_device_fail) })
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        logD("HomeFragment::onDestroy")
    }

}
