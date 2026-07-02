package com.jetcemetery.calculusPhoneNumber.calcOperation

import android.os.Handler
import android.util.Log
import java.util.ArrayList

class Singleton_MainLoop private constructor() {
    private var mainLoopHandler: Handler? = null
    private var pointsArray: IntArray? = null
    private var runnableList: MutableList<SecondaryForLoop>? = null

    fun pauseAllThreads() {
        Log.d(TAG, "pauseAllThreads")
        if (noActiveThreads()) {
            return
        }
        runnableList?.forEach { curTh ->
            if (curTh.getState() == SecondaryForLoop.STATE_RUNNING || curTh.getState() == SecondaryForLoop.STATE_NEW) {
                curTh.pause()
            }
        }
    }

    fun resumeAllThreads(updateUIHandler: Handler) {
        Log.d(TAG, "At start of resumeAllThreads")
        this.mainLoopHandler = updateUIHandler
        if (noActiveThreads()) {
            return
        }
        runnableList?.forEach { curTh ->
            if (curTh.getState() == SecondaryForLoop.STATE_PAUSED) {
                curTh.resume(updateUIHandler)
            }
        }
    }

    private fun noActiveThreads(): Boolean {
        return runnableList.isNullOrEmpty()
    }

    fun stopAllThreads() {
        Log.d(TAG, "At start of stopAllThreads")
        stopAllThreadsCalled = true
        runnableList?.forEach { it.gracefulExit() }
        runnableList = null
        instance = null
    }

    fun MainForLoopThread(data: Singleton_OperationValues, handler: Handler) {
        this.mainLoopHandler = handler
        pointsArray = createPointsArray(data)
    }

    fun StartProcess(data: Singleton_OperationValues) {
        runnableList = mutableListOf()
        pointsArray?.let { points ->
            for (opCount in 0 until points.size - 1) {
                createThread(points[opCount], points[opCount + 1], data)
            }
        }
    }

    private fun createThread(start: Int, end: Int, data: Singleton_OperationValues) {
        Log.d(TAG, "Creating thread for range: $start to $end")
        Thread {
            for (first_integral_value in start until end) {
                val secondObj = SecondaryForLoop(data, first_integral_value, mainLoopHandler)
                if (runnableList == null) {
                    break
                } else if (!stopAllThreadsCalled) {
                    runnableList?.add(secondObj)
                    secondObj.run()
                }
            }
        }.start()
    }

    private fun createPointsArray(data: Singleton_OperationValues): IntArray {
        val coresToUse = data.getThreadNum()
        val totalIterationRange = data.integral_1_End() - data.integral_1_Start()
        val incrementCount = totalIterationRange / coresToUse
        val returningArr = IntArray(coresToUse + 1)
        var tempIntegralStartPoint = data.integral_1_Start()
        returningArr[0] = tempIntegralStartPoint
        for (i in 1 until coresToUse) {
            tempIntegralStartPoint += incrementCount
            returningArr[i] = tempIntegralStartPoint
        }
        returningArr[coresToUse] = data.integral_1_End()
        Log.d(TAG, "Points array for distribution: ${returningArr.contentToString()}")
        return returningArr
    }

    companion object {
        private const val TAG = "Singleton_MainLoop"
        
        @JvmStatic
        var instance: Singleton_MainLoop? = null
            private set

        @Volatile
        private var stopAllThreadsCalled = false

        @JvmStatic
        fun initInstance() {
            if (instance == null) {
                instance = Singleton_MainLoop()
                stopAllThreadsCalled = false
            }
        }
    }
}
