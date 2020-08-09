package com.khanhlh.substationmonitor.model

import java.io.Serializable

data class PhongResponse(
    var errorCode: String? = null,
    var result: String? = null,
    var message: String? = null,
    var id: ArrayList<Phong>? = null
) : Serializable