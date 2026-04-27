package com.jetcemetery.calculusPhoneNumber.calcOperation

import kotlin.math.pow

class SingleMathOp(
    private val integrateOn: Int,
    private val integral_1: Int,
    private val integral_2: Int,
    private val integral_3: Int
) {
    private var mathOpResults: Long = 0

    init {
        runMath()
    }

    fun runMath() {
        val al = mathOp(integral_1, 4)
        val be = mathOp(integral_2, 3)
        val ga = mathOp(integral_3, 2)
        mathOpResults = al + be + ga
    }

    fun getDerivative(): Long = mathOpResults

    private fun mathOp(coEfficient: Int, expo: Int): Long {
        return (coEfficient.toDouble() * integrateOn.toDouble().pow(expo.toDouble())).toLong()
    }
}
