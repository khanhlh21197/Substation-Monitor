package com.khanhlh.substationmonitor.model

import com.google.firebase.firestore.PropertyName

data class Room(
    var id: String,
    @PropertyName("name")
    var name: String,
    var devices: String,
    @PropertyName("number_of_devices")
    var numberOfDevice: String
) {
    val number: String
        get() = "$numberOfDevice thiết bị"
}