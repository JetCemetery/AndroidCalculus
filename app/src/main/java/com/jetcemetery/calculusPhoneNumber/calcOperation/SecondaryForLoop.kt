package com.jetcemetery.calculusPhoneNumber.calcOperation

import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.jetcemetery.calculusPhoneNumber.activity.NewMainActivity
import com.jetcemetery.calculusPhoneNumber.helper.GetRandomInRange
import kotlin.math.abs

class SecondaryForLoop(
    private val userInput: Singleton_OperationValues,
    private val first_integral_value: Int,
    private var handler: Handler?
) : Runnable {

    @Volatile
    private var pauseWork = false

    @Volatile
    private var state = STATE_NEW

    private val targetNumber: Long = userInput.getParsedPhoneNumber()
    private val range: Int = 1000
    private val operatingInBatchMode: Boolean = true
    private var currentBatchAmountRange: Int = GetRandomInRange.getRandomNumberInRange(100, 1000)
    private var currentBatchAmount: Int = 0

    @Volatile
    private var stopProcess = false

    override fun run() {
        val movingIntegral_2 = userInput.integral_2_Start()
        val end_Integral_2 = userInput.integral_2_End()
        val movingIntegral_3 = userInput.integral_3_Start()
        val end_Integral_3 = userInput.integral_3_End()
        val rangeStart = userInput.xStart()
        val rangeEnd = userInput.xEnd()

        Log.d(TAG, "SecondaryForLoop started: First Integral Value: $first_integral_value, I2: $movingIntegral_2-$end_Integral_2, I3: $movingIntegral_3-$end_Integral_3")

        for (second_integral_value in movingIntegral_2 until end_Integral_2) {
            for (third_integral_value in movingIntegral_3 until end_Integral_3) {
                for (movingLowerLimit in rangeStart until rangeEnd) {
                    while (pauseWork) {
                        setState(STATE_PAUSED)
                        try {
                            Thread.sleep(1000)
                            if (stopProcess) {
                                gracefulExit_kill_thread()
                                return
                            }
                        } catch (ie: InterruptedException) {
                            Log.e(TAG, "Interrupt on secondary for loop!")
                            Log.e(TAG, ie.message ?: "")
                        }
                    }
                    setState(STATE_RUNNING)

                    val lowerLimitCalc = SingleMathOp(movingLowerLimit, first_integral_value, second_integral_value, third_integral_value)
                    val lowerLimitValue = lowerLimitCalc.getDerivative()
                    for (movingUpperLimit in (movingLowerLimit + 1)..rangeEnd) {
                        val mathOp = SingleMathOp(movingUpperLimit, first_integral_value, second_integral_value, third_integral_value)
                        val upperLimitValue = mathOp.getDerivative()
                        val diff = upperLimitValue - lowerLimitValue
                        if (abs(targetNumber - diff) < range) {
                            val dividend = targetNumber - diff
                            val phoneNumberTxt = PrintCalc.PrintCalcObjPass(movingLowerLimit, movingUpperLimit, first_integral_value, second_integral_value, third_integral_value, dividend)
                            postMessage("$phoneNumberTxt\n")
                            if (userInput.getStopOnFirstSuccess()) {
                                signalStop()
                            }
                        }
                        if (stopProcess) {
                            gracefulExit_kill_thread()
                            return
                        }
                    }
                    postCompleteOperation()
                }
                currentBatchAmount++
            }
        }

        if (operatingInBatchMode) {
            postCompleteOperationBatch(true)
        }
        setState(STATE_FINISHED)
    }

    private fun signalStop() {
        stopProcess = true
        handler?.let {
            val msg = it.obtainMessage()
            msg.what = NewMainActivity.SUCCESSFUL_OPERATION
            it.sendMessage(msg)
        }
    }

    private fun postCompleteOperation() {
        if (operatingInBatchMode) {
            postCompleteOperationBatch(false)
        } else {
            postCompleteOperationSingle()
        }
    }

    private fun postCompleteOperationSingle() {
        if (stopProcess) return
        handler?.let {
            val msg = it.obtainMessage()
            msg.what = NewMainActivity.ONE_CALCULATION_COMPLETED_BATCH
            it.sendMessage(msg)
        }
    }

    private fun postCompleteOperationBatch(forceSend: Boolean) {
        if (stopProcess) return
        currentBatchAmount++
        if (currentBatchAmount >= currentBatchAmountRange || forceSend) {
            if (forceSend) {
                currentBatchAmount--
            }
            handler?.let {
                val msg = it.obtainMessage()
                msg.what = NewMainActivity.ONE_CALCULATION_COMPLETED_BATCH
                val bundle = Bundle()
                bundle.putString(NewMainActivity.MESSAGE_NAME_ID, currentBatchAmount.toString())
                msg.data = bundle
                it.sendMessage(msg)
            }
            currentBatchAmount = 0
            currentBatchAmountRange = GetRandomInRange.getRandomNumberInRange(100, 1000)
        }
    }

    private fun postMessage(msgStr: String) {
        if (stopProcess) return
        handler?.let {
            val msg = it.obtainMessage()
            msg.what = NewMainActivity.POST_MESSAGE_IN_RESULTS
            val bundle = Bundle()
            bundle.putString(NewMainActivity.MESSAGE_NAME_ID, msgStr)
            msg.data = bundle
            it.sendMessage(msg)
        }
    }

    fun pause() {
        this.pauseWork = true
    }

    fun resume(mainLoopHandler: Handler) {
        this.handler = mainLoopHandler
        this.pauseWork = false
    }

    private fun setState(state: String) {
        this.state = state
    }

    fun getState(): String = this.state

    fun gracefulExit() {
        stopProcess = true
    }

    private fun gracefulExit_kill_thread() {
        this.handler = null
    }

    companion object {
        private const val TAG = "SecondaryForLoop"
        const val STATE_NEW = "New"
        const val STATE_PAUSED = "Paused"
        const val STATE_RUNNING = "Running"
        const val STATE_FINISHED = "Finished"
    }
}
