package com.khanhlh.substationmonitor.model

import java.io.Serializable

data class Nha(
    var idnha: String = "",
    val iduser: String = "",
    val tennha: String = "",
    val mac: String = "",
    val _id: String = ""
) : Serializable