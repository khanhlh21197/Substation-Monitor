package com.khanhlh.substationmonitor.exception

/**
 * Created by ditclear
 * 空数据异常，根据emptyType展示空页面
 */

class EmptyException(emptyType: Int) : Exception("empty")
