package com.khanhlh.substationmonitor.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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

    /**
     * Makes the View [View.INVISIBLE] unless the condition is met.
     */
    @Suppress("unused")
    @BindingAdapter("invisibleUnless")
    @JvmStatic fun invisibleUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Makes the View [View.GONE] unless the condition is met.
     */
    @Suppress("unused")
    @BindingAdapter("goneUnless")
    @JvmStatic fun goneUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Hides keyboard when the [EditText] is focused.
     *
     * Note that there can only be one [TextView.OnEditorActionListener] on each [EditText] and
     * this [BindingAdapter] sets it.
     */
    @BindingAdapter("hideKeyboardOnInputDone")
    @JvmStatic fun hideKeyboardOnInputDone(view: EditText, enabled: Boolean) {
        if (!enabled) return
        val listener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                view.clearFocus()
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            false
        }
        view.setOnEditorActionListener(listener)
    }

    @JvmStatic
    @BindingAdapter(value = ["onLongClick", "onLongClickText"], requireAll = true)
    fun setOnLongClickListener(view: View, onLongClick: (CharSequence) -> Unit, text: String?) {
        view.setOnLongClickListener {
            onLongClick.invoke(text?:"")
            true
        }
    }

    @JvmStatic
    @BindingAdapter("onLongClick")
    fun setOnLongClickListener(view: View, onLongClick: () -> Unit) {
        view.setOnLongClickListener {
            onLongClick.invoke()
            true
        }
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

    @BindingAdapter("isChecked")
    @JvmStatic fun setCheck(switch: SwitchButton, isChecked: Boolean) {
        switch.isChecked = isChecked
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