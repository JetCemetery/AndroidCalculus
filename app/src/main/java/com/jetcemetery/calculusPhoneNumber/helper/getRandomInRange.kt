package com.jetcemetery.calculusPhoneNumber.helper

import java.util.Random

object getRandomInRange {

    @JvmStatic
    fun getRandomNumberInRange(min: Int, max: Int): Int {
        return getRandomNumberInRangePrivate(min, max)
    }

    private fun getRandomNumberInRangePrivate(min: Int, max: Int): Int {
        if (min >= max) {
            throw IllegalArgumentException("max must be greater than min")
        }
        val r = Random()
        return r.nextInt(max - min + 1) + min
    }
}
