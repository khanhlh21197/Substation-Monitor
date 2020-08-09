package com.khanhlh.substationmonitor.model

import java.io.Serializable

data class BaseResponse(
    var errorCode: String? = null,
    var result: String? = null,
    var message: String? = null,
    var id: String? = null
) : Serializable