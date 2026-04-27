package com.jetcemetery.calculusPhoneNumber.calcOperation

import android.util.Log
import com.jetcemetery.calculusPhoneNumber.helper.GettingUserPhoneNumber
import com.jetcemetery.calculusPhoneNumber.helper.getCPU_Cnt
import java.util.ArrayList

class Singleton_OperationValues(
    private var rawPhoneNumber: String?,
    private var integral_1_sta: Int,
    private var integral_2_sta: Int,
    private var integral_3_sta: Int,
    private var integral_1_end: Int,
    private var integral_2_end: Int,
    private var integral_3_end: Int,
    private var stopOnFirstSuccess: Boolean
) {
    private var cpuOptions: CpuUseOptions? = null
    private var cpusOnDevice: Int = 0
    private var cpuToUse: Int = 0
    private var expectedOperations: Float = 0f
    private var changesMade = false
    private var localProgressOperationsCompleted: Long = 0
    private var resultsTextArea: String? = null
    private var resultsList: ArrayList<String>? = null

    init {
        initInternal()
    }

    private fun initInternal() {
        cpuOptions = CpuUseOptions.CPU_ALL
        val findCPUCnt = getCPU_Cnt()
        cpusOnDevice = findCPUCnt.count
        cpusToUsePopulate()
        localProgressOperationsCompleted = 0
        changesMade = false
        updateTotalExpectedOperations()
        resultsTextArea = "Results"
    }

    enum class CpuUseOptions {
        CPU_SINGLE,
        CPU_HALF,
        CPU_ALL_MINUS_1,
        CPU_ALL
    }

    fun setTextArea(textArea: String?) {
        Log.d(TAG, "calling setTextArea, passed [$textArea]")
        resultsTextArea = textArea
    }

    fun getTextArea(): String? = resultsTextArea

    fun getPhoneNumber(): String? = rawPhoneNumber

    fun clearChangesMadeLatch() {
        changesMade = false
    }

    fun changesMade() {
        Log.d(TAG, "changesMade tripped")
        changesMade = true
    }

    fun wasDataChanged(): Boolean {
        if (changesMade) {
            updateTotalExpectedOperations()
            localProgressOperationsCompleted = 0
            setTextArea("")
        }
        return changesMade
    }

    fun getParsedPhoneNumber(): Long {
        val buildingData = ArrayList<Char>()
        rawPhoneNumber?.let {
            for (c in it.toCharArray()) {
                if (c.isDigit()) {
                    buildingData.add(c)
                }
            }
        }
        val returningStr = StringBuilder()
        for (c in buildingData) {
            returningStr.append(c)
        }
        return try {
            returningStr.toString().toLong()
        } catch (e: NumberFormatException) {
            0L
        }
    }

    fun setProgressCount(currentOperationsCompleted: Long) {
        localProgressOperationsCompleted = currentOperationsCompleted
    }

    fun getCurrentProgressCount(): Long = localProgressOperationsCompleted

    fun setArrayList(resultsList: ArrayList<String>?) {
        this.resultsList = resultsList
    }

    fun getTextArray(): ArrayList<String>? = resultsList

    fun nu_GetDataList(): Array<String> {
        if (resultsList == null) {
            resultsList = ArrayList()
        }
        return resultsList!!.toTypedArray()
    }

    fun nu_saveData(list: Array<String>) {
        if (resultsList == null) {
            resultsList = ArrayList()
        }
        resultsList!!.clear()
        for (str in list) {
            resultsList!!.add(str)
        }
    }

    fun setIntegralRanges(start1: Int, start2: Int, start3: Int, end1: Int, end2: Int, end3: Int) {
        integral_1_sta = start1
        integral_2_sta = start2
        integral_3_sta = start3
        integral_1_end = end1
        integral_2_end = end2
        integral_3_end = end3
    }

    @Synchronized
    fun setPhoneNumber(srcPhoneNum: String?) {
        rawPhoneNumber = srcPhoneNum
    }

    fun setCpu_single() {
        cpuOptions = CpuUseOptions.CPU_SINGLE
        cpusToUsePopulate()
    }

    fun setCpu_half() {
        cpuOptions = CpuUseOptions.CPU_HALF
        cpusToUsePopulate()
    }

    fun setCpu_all_m_1() {
        cpuOptions = CpuUseOptions.CPU_ALL_MINUS_1
        cpusToUsePopulate()
    }

    fun setCpu_all() {
        cpuOptions = CpuUseOptions.CPU_ALL
        cpusToUsePopulate()
    }

    fun updateTotalExpectedOperations() {
        var tempVal = 1L
        val int1 = integral_1_end - integral_1_sta
        val int2 = integral_2_end - integral_2_sta
        val int3 = integral_3_end - integral_3_sta
        val lastPart = 1000
        tempVal *= int1.toLong()
        tempVal *= int2.toLong()
        tempVal *= int3.toLong()
        tempVal *= lastPart.toLong()
        expectedOperations = tempVal.toFloat()
    }

    private fun cpusToUsePopulate() {
        changesMade()
        cpuToUse = when (cpuOptions) {
            CpuUseOptions.CPU_HALF -> cpusOnDevice / 2
            CpuUseOptions.CPU_ALL_MINUS_1 -> cpusOnDevice - 1
            CpuUseOptions.CPU_ALL -> cpusOnDevice
            CpuUseOptions.CPU_SINGLE -> 1
            else -> 1
        }
        if (cpuToUse <= 0) cpuToUse = 1
    }

    fun getThreadNum(): Int = cpuToUse

    fun integral_1_Start(): Int = integral_1_sta

    fun integral_1_End(): Int = integral_1_end

    fun integral_2_Start(): Int = integral_2_sta

    fun integral_2_End(): Int = integral_2_end

    fun integral_3_Start(): Int = integral_3_sta

    fun integral_3_End(): Int = integral_3_end

    fun xStart(): Int = 1

    fun xEnd(): Int = 1000

    fun operationForProgressBar(): Int = operationForProgressBar(localProgressOperationsCompleted.toFloat())

    fun operationForProgressBar(completedOperations: Float): Int {
        var temp = completedOperations / expectedOperations
        temp *= 100f
        return temp.toInt()
    }

    fun getTotalOperationExpected(): String {
        return expectedOperations.toLong().toString()
    }

    fun setStopOnSuccess(value: Boolean) {
        changesMade()
        stopOnFirstSuccess = value
    }

    fun getStopOnFirstSuccess(): Boolean = stopOnFirstSuccess

    fun getCPU_OptionsEnum(): CpuUseOptions? = cpuOptions

    companion object {
        private const val TAG = "OperationValues"
        @JvmStatic
        var instance: Singleton_OperationValues? = null
            private set

        @JvmStatic
        fun initInstance() {
            if (instance == null) {
                val INTEGRAL_START_DEFAULT = 1
                val INTEGRAL_END_DEFAULT = 100
                val defaultNumber = "555-555-5555"
                var phoneNumber: String? = try {
                    val phoneHelper = GettingUserPhoneNumber()
                    val UserRealNumber = phoneHelper.userNumber
                    if (UserRealNumber.length <= 1) defaultNumber else UserRealNumber
                } catch (e: Exception) {
                    defaultNumber
                }

                instance = Singleton_OperationValues(
                    phoneNumber,
                    INTEGRAL_START_DEFAULT,
                    INTEGRAL_START_DEFAULT,
                    INTEGRAL_START_DEFAULT,
                    INTEGRAL_END_DEFAULT,
                    INTEGRAL_END_DEFAULT,
                    INTEGRAL_END_DEFAULT,
                    false
                )
                instance!!.resultsList = ArrayList()
            }
        }
    }
}
