package com.khanhlh.substationmonitor.helper.dialog

import android.app.Activity
import android.app.Dialog
import android.view.View
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.khanhlh.substationmonitor.extensions.logD

inline fun Activity.alert(
    act: Activity, messageId: Int, titleId: Int,
    leftButtonTextId: Int, rightButtonTextId: Int,
    leftClick: View.OnClickListener?, rightClick: View.OnClickListener?
): Dialog? {
    return try {
        MaterialDialog.Builder(act)
            .title(act.getString(titleId))
            .content(act.getString(messageId))
            .positiveText(act.getString(rightButtonTextId))
            .negativeText(act.getString(leftButtonTextId))
            .onPositive { dialog, which ->
                rightClick?.onClick(act.currentFocus)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            .onNegative { dialog, which ->
                leftClick?.onClick(act.currentFocus)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }.build()
    } catch (e: Exception) {
        logD(e.toString())
        null
    }
}

inline fun Fragment.alert(
    frag: Fragment, messageId: Int, titleId: Int,
    leftButtonTextId: Int, rightButtonTextId: Int,
    leftClick: View.OnClickListener?, rightClick: View.OnClickListener?
): Dialog? {
    return try {
        MaterialDialog.Builder(frag.requireActivity())
            .title(frag.requireActivity().getString(titleId))
            .content(frag.requireActivity().getString(messageId))
            .positiveText(frag.requireActivity().getString(rightButtonTextId))
            .negativeText(frag.requireActivity().getString(leftButtonTextId))
            .onPositive { dialog, which ->
                rightClick?.onClick(frag.requireActivity().currentFocus)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            .onNegative { dialog, which ->
                leftClick?.onClick(frag.requireActivity().currentFocus)
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }.build()
    } catch (e: Exception) {
        logD(e.toString())
        null
    }
}