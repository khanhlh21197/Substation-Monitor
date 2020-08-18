package com.khanhlh.substationmonitor

import android.app.Application
import android.content.Context

class MyApp : Application() {
    companion object {
        var mContext: Context? = null
        fun applicationContext(): Context {
            return mContext!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
    }
}