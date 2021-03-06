package com.khanhlh.substationmonitor.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.extensions.dispatchFailure
import com.khanhlh.substationmonitor.extensions.toast
import com.khanhlh.substationmonitor.helper.annotation.ToastType
import com.khanhlh.substationmonitor.ui.main.MainViewModel


/**
 * 页面描述：fragment 基类
 *
 * Created by ditclear on 2017/9/27.
 */

abstract class BaseFragment<VB : ViewDataBinding, T : BaseViewModel<*>> : Fragment() {
    protected val mBinding: VB by lazy {
        DataBindingUtil.inflate<VB>(
            layoutInflater,
            getLayoutId(),
            null,
            false
        )
    }

    lateinit var vm: T

    private lateinit var errorSnackbar: Snackbar

    private lateinit var viewModel: MainViewModel

    protected lateinit var mContext: Context

    private lateinit var progressDialog: Dialog

    protected var lazyLoad = false

    protected var visible = false

    /**
     * 标志位，标志已经初始化完成
     */
    protected var isPrepared: Boolean = false

    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    protected var hasLoadOnce: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initArgs(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mContext = activity ?: throw Exception("activity 为null")
        retainInstance = true
        initView()
        observeViewModel()
        if (lazyLoad) {
            //延迟加载，需重写lazyLoad方法
            lazyLoad()
        } else {
            // 加载数据
//            loadData(true);
        }
        updateTitle(title)
    }

    private fun updateTitle(title: String) {
        activity?.run {
            viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        } ?: throw Throwable("invalid activity")
        viewModel.updateActionBarTitle(title)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        mBinding.setVariable(BR.vm, this)
        mBinding.executePendingBindings()
        mBinding.lifecycleOwner = this

        progressDialog = Dialog(requireActivity())
        progressDialog.apply {
            window?.let {
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.requestFeature(Window.FEATURE_NO_TITLE)
            }
            setContentView(R.layout.progress_dialog)
            setCancelable(true)
            setCanceledOnTouchOutside(false)
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * 是否可见，延迟加载
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (userVisibleHint) {
            visible = true
            onVisible()
        } else {
            visible = false
            onInvisible()
        }
    }

    protected fun onInvisible() {

    }

    protected open fun onVisible() {
        lazyLoad()
    }


    open fun lazyLoad() {}

    open fun initArgs(savedInstanceState: Bundle?) {

    }

    abstract fun initView()

    abstract fun getLayoutId(): Int

    open val title: String = ""

    fun toast(msg: String) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
    }

    fun toast(msg: Int) {
        Toast.makeText(mContext, mContext.getString(msg), Toast.LENGTH_SHORT).show()
    }

    fun toastSuccess(msg: String?) {
        msg?.let { activity?.toast(it, ToastType.SUCCESS) }
    }

    fun toastFailure(error: Throwable) {
        activity?.dispatchFailure(error)
    }

    protected fun <T> autoWired(key: String, default: T? = null): T? =
        arguments?.let { findWired(it, key, default) }

    private fun <T> findWired(bundle: Bundle, key: String, default: T? = null): T? {
        return if (bundle.get(key) != null) {
            try {
                bundle.get(key) as T
            } catch (e: ClassCastException) {
                e.printStackTrace()
                null
            }
        } else default

    }

    protected fun hasPermission(perm: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireActivity(),
            perm
        )
    }

    protected fun hasPermissions(permList: List<String>): Boolean {
        var bool: Boolean
        for (item in permList) {
            bool = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                item
            )
            if (!bool) return false
        }
        return true
    }

    protected fun hasPermissions(vararg perm: String): Boolean {
        var bool: Boolean
        for (item in perm) {
            bool = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                item
            )
            if (!bool) return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    protected fun requestPermissions(perm: String, requestCode: Int): Unit {
        requireActivity().requestPermissions(arrayOf(perm), requestCode)
    }

    @SuppressLint("FragmentLiveDataObserve")
    open fun observeViewModel() {
        vm.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            showError(errorMessage)
        })
        vm.loadingVisibility.observe(this, Observer { visibility ->
            if (visibility == View.VISIBLE) showLoading() else hideLoading()
        })
    }

    override fun onDestroy() {
        vm.detachView()
        super.onDestroy()
    }

    private fun showError(error: String) {
        errorSnackbar = Snackbar.make(mBinding.root, error, Snackbar.LENGTH_LONG)
        errorSnackbar.show()
    }

    fun hideLoading() {

        progressDialog.dismiss()
    }

    fun showLoading() {

        progressDialog.show()

    }
}