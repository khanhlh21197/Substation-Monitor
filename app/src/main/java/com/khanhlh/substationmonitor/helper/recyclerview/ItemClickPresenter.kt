package com.khanhlh.substationmonitor.helper.recyclerview

import android.view.View
import androidx.databinding.ViewDataBinding


/**
 * 页面描述：
 *
 * Created by ditclear on 2017/9/28.
 */
interface ItemClickPresenter<in Any> {
    fun onItemClick(v: View? = null, item: Any)
    fun onItemLongClick(v: View? = null, item: Any)
    fun onImageClick(v: View? = null)
    fun onDeleteClick(v: View? = null, item: Any)
    fun onSwitchChange(isChecked: Boolean): Boolean
}

interface ItemDecorator {
    fun decorator(holder: BindingViewHolder<ViewDataBinding>?, position: Int, viewType: Int)
}

interface ItemAnimator {

    fun scrollUpAnim(v: View)

    fun scrollDownAnim(v: View)
}