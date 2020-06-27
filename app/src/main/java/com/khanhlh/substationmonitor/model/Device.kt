package com.khanhlh.substationmonitor.model

import android.graphics.Bitmap
import com.google.firebase.firestore.PropertyName

data class Device(
    val id: String,
    @PropertyName("name")
    val name: String,
    @PropertyName("temp")
    val temp: String,
    val image: Bitmap? = null
)