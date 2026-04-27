package com.jetcemetery.calculusPhoneNumber.helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class GettingUserPhoneNumber : AppCompatActivity() {

    private val TAG = "GettingUserPhoneNumber"
    private var returningNumber: String = "555-555-5555"
    private val wantPermission = Manifest.permission.READ_PHONE_STATE

    init {
        Log.d(TAG, "At Start of getting Phone Number")
    }

    val userNumber: String
        get() {
            Log.d(TAG, "getUserNumber is called")
            val tempStr = getPhone()
            if (!tempStr.isNullOrEmpty()) {
                returningNumber = tempStr
            }
            return returningNumber
        }

    @SuppressLint("HardwareIds")
    private fun getPhone(): String? {
        val phoneMgr = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        if (ActivityCompat.checkSelfPermission(this, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return ""
        }
        @Suppress("DEPRECATION")
        return phoneMgr?.line1Number
    }

    private fun requestPermission(permission: String) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(this, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Phone number: " + getPhone())
                } else {
                    Toast.makeText(this, "Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            result == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}
