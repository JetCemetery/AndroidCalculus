package com.jetcemetery.calculusPhoneNumber

import android.app.Application
import android.content.Context

class CalculusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: CalculusApp? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }
}
