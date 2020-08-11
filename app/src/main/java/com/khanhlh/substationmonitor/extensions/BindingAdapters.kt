package com.khanhlh.substationmonitor.extensions

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.khanhlh.substationmonitor.helper.recyclerview.ItemClickPresenter
import com.suke.widget.SwitchButton

@SuppressLint("CheckResult")
object BindingAdapters {

    @JvmStatic
    @BindingAdapter(value = ["imageUrl", "placeholder"], requireAll = false)
    fun setImageUrl(imageView: ImageView, url: String?, placeHolder: Drawable?) {
        imageView.setImageDrawable(placeHolder)
    }

    private val INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()

    @JvmStatic
    @BindingAdapter("isFlashing")
    fun setFlashing(view: View, isFlash: Boolean) {
        if (isFlash) {
            view.startAnimation(createFlashingAnimation())
        } else {
            if (view.animation != null)
                view.animation.cancel()
        }
    }

    @JvmStatic
    @BindingAdapter("onSwitchChange")
    fun onSwitchChange(switch: SwitchButton, item: ItemClickPresenter<Any>) {
        switch.setOnCheckedChangeListener { view, isChecked ->
            item.onSwitchChange(isChecked)
        }
    }

    private fun createFlashingAnimation(): Animation? {
        val flashingAnimation: Animation =
            AlphaAnimation(1F, 0F) // Change alpha from fully visible to invisible
        flashingAnimation.duration = 500 // duration - half a second
        flashingAnimation.interpolator = LinearInterpolator() // do not alter animation rate
        flashingAnimation.repeatCount = Animation.INFINITE // Repeat animation infinitely
        flashingAnimation.repeatMode =
            Animation.REVERSE // Reverse animation at the end so the button will fade back in
        return flashingAnimation
    }

    private fun createAnimation(): Animation {
        val anim = RotateAnimation(
            0F,
            360F,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        anim.interpolator = INTERPOLATOR
        anim.duration = 1400
        anim.repeatCount = TranslateAnimation.INFINITE
        anim.repeatMode = TranslateAnimation.RESTART
        return anim
    }

}