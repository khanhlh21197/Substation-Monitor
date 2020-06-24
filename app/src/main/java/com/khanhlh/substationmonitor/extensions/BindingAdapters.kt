package com.khanhlh.substationmonitor.extensions

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@SuppressLint("CheckResult")
object BindingAdapters {

    @JvmStatic
    @BindingAdapter(value = ["imageUrl", "placeholder"], requireAll = false)
    fun setImageUrl(imageView: ImageView, url: String?, placeHolder: Drawable?) {
        imageView.setImageDrawable(placeHolder)
    }


}