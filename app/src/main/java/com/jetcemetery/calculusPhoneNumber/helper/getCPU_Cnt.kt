package com.jetcemetery.calculusPhoneNumber.helper

import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileFilter
import java.io.Serializable
import java.util.regex.Pattern

class getCPU_Cnt : Serializable {

    val count: Int
        get() = numberOfCores

    private val numberOfCores: Int
        get() {
            return if (Build.VERSION.SDK_INT >= 17) {
                Runtime.getRuntime().availableProcessors()
            } else {
                numCoresOldPhones
            }
        }

    private val numCoresOldPhones: Int
        get() {
            class CpuFilter : FileFilter {
                override fun accept(pathname: File): Boolean {
                    return Pattern.matches("cpu[0-9]+", pathname.name)
                }
            }

            return try {
                val dir = File("/sys/devices/system/cpu/")
                val files = dir.listFiles(CpuFilter())
                files?.size ?: 1
            } catch (e: Exception) {
                Log.d(TAG, "CPU Count: Failed.")
                e.printStackTrace()
                1
            }
        }

    companion object {
        private const val TAG = "GetCPU_Cnt"
    }
}
