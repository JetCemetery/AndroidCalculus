package com.jetcemetery.calculusPhoneNumber.helper

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.jetcemetery.calculusPhoneNumber.CalculusApp
import kotlin.random.Random

object GetRandomInRange {

    private var lastToastTime: Long = 0
    private const val TOAST_COOLDOWN_MS = 10000L

    @JvmStatic
    fun getRandomNumberInRange(min: Int, max: Int): Int {
        return getRandomNumberInRangePrivate(min, max)
    }

    private fun getRandomNumberInRangePrivate(min: Int, max: Int): Int {
        var finalMin = min
        var finalMax = max

        if (finalMin >= finalMax) {
            finalMin = 1
            finalMax = 100
            
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastToastTime > TOAST_COOLDOWN_MS) {
                lastToastTime = currentTime
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(CalculusApp.getContext(), "numbers inverted", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        return Random.nextInt(finalMax - finalMin + 1) + finalMin
    }
}
