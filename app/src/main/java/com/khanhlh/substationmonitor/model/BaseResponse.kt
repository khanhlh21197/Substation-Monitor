package com.khanhlh.substationmonitor.model

data class BaseResponse(
    var errorCode: String? = null,
    var result: String? = null,
    var message: String? = null
)