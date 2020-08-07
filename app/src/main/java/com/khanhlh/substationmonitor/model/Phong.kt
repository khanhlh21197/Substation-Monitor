package com.khanhlh.substationmonitor.model

data class Phong(
    var id: String = "",
    val tenphong: String = "",
    val idNha: String = "",
    val thietBiS: ArrayList<ThietBi> = ArrayList()
)