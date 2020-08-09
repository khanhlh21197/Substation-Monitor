package com.khanhlh.substationmonitor.model

import java.io.Serializable

data class ThietBiResponse(
    var errorCode: String? = null,
    var result: String? = null,
    var message: String? = null,
    var id: ArrayList<ThietBi>? = null,
    var _id: String? = null
) : Serializable