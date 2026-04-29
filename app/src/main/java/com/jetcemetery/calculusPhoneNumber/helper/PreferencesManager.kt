package com.jetcemetery.calculusPhoneNumber.helper

import android.content.Context
import android.content.SharedPreferences
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("CalculusPrefs", Context.MODE_PRIVATE)

    fun saveSettings(
        start1: Int, start2: Int, start3: Int,
        end1: Int, end2: Int, end3: Int,
        cpuOption: Singleton_OperationValues.CpuUseOptions,
        stopOnSuccess: Boolean
    ) {
        sharedPreferences.edit().apply {
            putInt("start1", start1)
            putInt("start2", start2)
            putInt("start3", start3)
            putInt("end1", end1)
            putInt("end2", end2)
            putInt("end3", end3)
            putString("cpuOption", cpuOption.name)
            putBoolean("stopOnSuccess", stopOnSuccess)
            apply()
        }
    }

    fun loadSettings(dataObj: Singleton_OperationValues) {
        val start1 = sharedPreferences.getInt("start1", 1)
        val start2 = sharedPreferences.getInt("start2", 1)
        val start3 = sharedPreferences.getInt("start3", 1)
        val end1 = sharedPreferences.getInt("end1", 100)
        val end2 = sharedPreferences.getInt("end2", 100)
        val end3 = sharedPreferences.getInt("end3", 100)
        val cpuOptionName = sharedPreferences.getString("cpuOption", Singleton_OperationValues.CpuUseOptions.CPU_ALL.name)
        val stopOnSuccess = sharedPreferences.getBoolean("stopOnSuccess", false)

        dataObj.setIntegralRanges(start1, start2, start3, end1, end2, end3)
        
        val cpuOption = try {
            Singleton_OperationValues.CpuUseOptions.valueOf(cpuOptionName ?: Singleton_OperationValues.CpuUseOptions.CPU_ALL.name)
        } catch (e: IllegalArgumentException) {
            Singleton_OperationValues.CpuUseOptions.CPU_ALL
        }

        when (cpuOption) {
            Singleton_OperationValues.CpuUseOptions.CPU_SINGLE -> dataObj.setCpu_single()
            Singleton_OperationValues.CpuUseOptions.CPU_HALF -> dataObj.setCpu_half()
            Singleton_OperationValues.CpuUseOptions.CPU_ALL_MINUS_1 -> dataObj.setCpu_all_m_1()
            Singleton_OperationValues.CpuUseOptions.CPU_ALL -> dataObj.setCpu_all()
        }
        
        dataObj.setStopOnSuccess(stopOnSuccess)
        dataObj.updateTotalExpectedOperations()
    }
}
