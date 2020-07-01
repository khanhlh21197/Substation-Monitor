package com.khanhlh.substationmonitor.helper.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.google.android.material.textfield.TextInputEditText
import com.khanhlh.substationmonitor.R

class NotesDialogHelper(context: Context) : BaseDialogHelper() {

    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.notes_dialog, null)
    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)

    //  notes edit text
    val eText: TextInputEditText by lazy {
        dialogView.findViewById<TextInputEditText>(R.id.notes_etxt_view)
    }

    //  done icon
    private val doneIcon: ImageView by lazy {
        dialogView.findViewById<ImageView>(R.id.done_icon)
    }

    //  close icon
    private val closeIcon: ImageView by lazy {
        dialogView.findViewById<ImageView>(R.id.close_icon)
    }

    //  closeIconClickListener with listener
    fun closeIconClickListener(func: (() -> Unit)? = null) =
        with(closeIcon) {
            setClickListenerToDialogIcon(func)
        }

    //  doneIconClickListener with listener
    fun doneIconClickListener(func: (() -> Unit)? = null) =
        with(doneIcon) {
            setClickListenerToDialogIcon(func)
        }

    //  view click listener as extension function
    private fun View.setClickListenerToDialogIcon(func: (() -> Unit)?) =
        setOnClickListener {
            func?.invoke()
            dialog?.dismiss()
        }
}