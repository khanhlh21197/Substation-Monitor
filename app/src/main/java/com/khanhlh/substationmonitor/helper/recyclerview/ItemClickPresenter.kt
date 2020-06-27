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
    fun onImageClick(v: View? = null)
}

interface ItemDecorator {
    fun decorator(holder: BindingViewHolder<ViewDataBinding>?, position: Int, viewType: Int)
}

interface ItemAnimator {

    fun scrollUpAnim(v: View)

    fun scrollDownAnim(v: View)
}