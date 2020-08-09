package com.khanhlh.substationmonitor.model

import java.io.Serializable

data class NhaResponse(
    var errorCode: String? = null,
    var result: String? = null,
    var message: String? = null,
    var id: ArrayList<Nha>? = null,
    var _id : String? = null
) : Serializable