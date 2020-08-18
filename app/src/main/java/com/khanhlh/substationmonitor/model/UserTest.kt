package com.khanhlh.substationmonitor.model

import java.io.Serializable

data class UserTest(
    val email: String = "",
    val pass: String = "",
    val ten: String = "",
    val sdt: String = "",
    val nha: String = "",
    val mac: String = ""
) : Serializable