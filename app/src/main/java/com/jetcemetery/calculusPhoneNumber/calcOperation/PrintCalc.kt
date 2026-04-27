package com.jetcemetery.calculusPhoneNumber.calcOperation

import kotlin.math.abs

object PrintCalc {

    private fun mathOp2(dervValue: Long, targetNum: Long): Long {
        return abs(targetNum - dervValue)
    }

    private fun printResult(lowerX: Int, upperX: Int, alpha: Int, beta: Int, gamma: Int, dividend: Long, divisor: Int): String {
        val builder = StringBuilder()
        builder.append("The integral of ")
        builder.append(lowerX)
        builder.append(" to ")
        builder.append(upperX)
        builder.append(" of (")
        builder.append(alpha * 4)
        builder.append("x^3 +")
        builder.append(beta * 3)
        builder.append("x^2 +")
        builder.append(gamma * 2)
        if (dividend >= 0) {
            builder.append("x +")
        } else {
            builder.append("x ")
        }
        builder.append(dividend)
        builder.append("/")
        builder.append(divisor)
        builder.append(") dx")
        return builder.toString()
    }

    @JvmStatic
    fun PrintCalcObjPass(xlow: Int, xhigh: Int, alpha: Int, beta: Int, gamma: Int, diff: Long): String {
        val divBot = getDivisor(xlow, xhigh)
        return printResult(xlow, xhigh, alpha, beta, gamma, diff, divBot)
    }

    private fun getDivisor(xlow: Int, xhigh: Int): Int {
        return xhigh - xlow
    }
}
