package com.khanhlh.substationmonitor.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.khanhlh.substationmonitor.R


abstract class BaseActivity<B : ViewDataBinding, T : BaseViewModel<*>> :
    AppCompatActivity() {
    lateinit var baseDataBinding: B
    lateinit var baseViewModel: T
    private lateinit var errorSnackbar: Snackbar
    private lateinit var progressDialog: Dialog

    protected fun bindView(layoutId: Int) {
        baseDataBinding = DataBindingUtil.setContentView(this, layoutId)
    }

    abstract fun initVariables()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVariables()
        observeViewModel()

        progressDialog = Dialog(this)
        progressDialog.apply {
            window?.let {
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.requestFeature(Window.FEATURE_NO_TITLE)
            }
            setContentView(R.layout.progress_dialog)
            setCancelable(true)
            setCanceledOnTouchOutside(false)
        }


    }

    open fun observeViewModel() {
        baseViewModel.errorMessage.observe(this, Observer { errorMessage ->
            showError(errorMessage)
        })
        baseViewModel.loadingVisibility.observe(this, Observer { visibility ->
            if (visibility == View.VISIBLE) showLoading() else hideLoading()
        })
    }

    private fun showError(error: String) {
        errorSnackbar = Snackbar.make(baseDataBinding.root, error, Snackbar.LENGTH_LONG)
        errorSnackbar.show()
    }

    override fun onDestroy() {
        baseViewModel.detachView()
        super.onDestroy()
    }


    private fun hideLoading() {

        progressDialog.dismiss()
    }

    private fun showLoading() {

        progressDialog.show()

    }
}