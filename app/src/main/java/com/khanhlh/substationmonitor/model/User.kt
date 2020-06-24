package com.khanhlh.substationmonitor.model

import com.google.firebase.firestore.PropertyName

data class User(
    @PropertyName("id")
    val id: String,
    @PropertyName("email")
    val email: String,
    @PropertyName("deviceList")
    val deviceList: String
)