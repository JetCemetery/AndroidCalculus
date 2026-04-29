package com.jetcemetery.calculusPhoneNumber.calcOperation

import kotlin.math.abs

object PrintCalc {

    private fun mathOp2(dervValue: Long, targetNum: Long): Long {
        return abs(targetNum - dervValue)
    }

    private fun printResult(
        lowerX: Int,
        upperX: Int,
        alpha: Int,
        beta: Int,
        gamma: Int,
        dividend: Long,
        divisor: Int
    ): String {
        val sign = if (dividend >= 0) "+" else ""

        return "The integral of $lowerX to $upperX of (${alpha * 4}x^3 +${beta * 3}x^2 +${gamma * 2}x $sign$dividend/$divisor) dx"
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
