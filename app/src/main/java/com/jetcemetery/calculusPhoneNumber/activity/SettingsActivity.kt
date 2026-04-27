package com.jetcemetery.calculusPhoneNumber.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.NumberPicker
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.jetcemetery.androidcalculus.R
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues

class SettingsActivity : AppCompatActivity() {

    private lateinit var integral1_start: NumberPicker
    private lateinit var integral2_start: NumberPicker
    private lateinit var integral3_start: NumberPicker
    private lateinit var integral1_end: NumberPicker
    private lateinit var integral2_end: NumberPicker
    private lateinit var integral3_end: NumberPicker
    
    private lateinit var cpu_cnt_Single: RadioButton
    private lateinit var cpu_cnt_half: RadioButton
    private lateinit var cpu_cnt_all_minus1: RadioButton
    private lateinit var cpu_cnt_all: RadioButton
    
    private lateinit var stopOnSuccess_Yes: RadioButton
    private lateinit var stopOnSuccess_No: RadioButton
    
    private var localDataObj: Singleton_OperationValues? = null

    private val numberPicker_setMinValue = 1
    private val numberPicker_setMaxValue = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        Log.d(TAG, "Start of onCreate")
        
        integral1_start = findViewById(R.id.integral1_start)
        integral2_start = findViewById(R.id.integral2_start)
        integral3_start = findViewById(R.id.integral3_start)

        integral1_end = findViewById(R.id.integral1_end)
        integral2_end = findViewById(R.id.integral2_end)
        integral3_end = findViewById(R.id.integral3_end)
        
        cpu_cnt_Single = findViewById(R.id.cpu_cnt_Single)
        cpu_cnt_half = findViewById(R.id.cpu_cnt_half)
        cpu_cnt_all_minus1 = findViewById(R.id.cpu_cnt_all_minus1)
        cpu_cnt_all = findViewById(R.id.cpu_cnt_all)
        
        stopOnSuccess_Yes = findViewById(R.id.stopOnSuccess_Yes)
        stopOnSuccess_No = findViewById(R.id.stopOnSuccess_No)

        initDataObjectSingleton()
        initUI()
    }

    private fun initDataObjectSingleton() {
        Singleton_OperationValues.initInstance()
        localDataObj = Singleton_OperationValues.instance
    }

    private fun initUI() {
        setStartIntegral(integral1_start)
        setStartIntegral(integral2_start)
        setStartIntegral(integral3_start)
        setEndIntegral(integral1_end)
        setEndIntegral(integral2_end)
        setEndIntegral(integral3_end)

        val dataObj = localDataObj ?: return

        when (dataObj.getCPU_OptionsEnum()) {
            Singleton_OperationValues.CpuUseOptions.CPU_HALF -> cpu_cnt_half.isChecked = true
            Singleton_OperationValues.CpuUseOptions.CPU_ALL_MINUS_1 -> cpu_cnt_all_minus1.isChecked = true
            Singleton_OperationValues.CpuUseOptions.CPU_ALL -> cpu_cnt_all.isChecked = true
            Singleton_OperationValues.CpuUseOptions.CPU_SINGLE -> cpu_cnt_Single.isChecked = true
            else -> cpu_cnt_Single.isChecked = true
        }

        if (dataObj.getStopOnFirstSuccess()) {
            stopOnSuccess_Yes.isChecked = true
        } else {
            stopOnSuccess_No.isChecked = true
        }

        cpu_cnt_Single.setOnClickListener { dataObj.setCpu_single() }
        cpu_cnt_half.setOnClickListener { dataObj.setCpu_half() }
        cpu_cnt_all_minus1.setOnClickListener { dataObj.setCpu_all_m_1() }
        cpu_cnt_all.setOnClickListener { dataObj.setCpu_all() }

        stopOnSuccess_Yes.setOnClickListener { dataObj.setStopOnSuccess(true) }
        stopOnSuccess_No.setOnClickListener { dataObj.setStopOnSuccess(false) }

        integral1_start.value = dataObj.integral_1_Start()
        integral2_start.value = dataObj.integral_2_Start()
        integral3_start.value = dataObj.integral_3_Start()

        integral1_end.value = dataObj.integral_1_End()
        integral2_end.value = dataObj.integral_2_End()
        integral3_end.value = dataObj.integral_3_End()
    }

    private fun setStartIntegral(srcIntegral: NumberPicker) {
        srcIntegral.minValue = numberPicker_setMinValue
        srcIntegral.maxValue = numberPicker_setMaxValue
        srcIntegral.setOnValueChangedListener { _, _, _ -> localDataObj?.changesMade() }
    }

    private fun setEndIntegral(srcIntegral: NumberPicker) {
        srcIntegral.minValue = numberPicker_setMinValue
        srcIntegral.maxValue = numberPicker_setMaxValue
        srcIntegral.setOnValueChangedListener { _, _, _ -> localDataObj?.changesMade() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == R.id.menu_settings) {
                menu.getItem(i).isVisible = false
                break
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.menu_about -> {
                Log.d(TAG, "Menu button hit, going to menu_about")
                intent = Intent(applicationContext, AboutActivity::class.java)
                saveDataObjState()
                startActivity(intent)
            }
            R.id.menu_home -> {
                Log.d(TAG, "Menu button hit, going to menu_home")
                intent = Intent(applicationContext, MainActivity::class.java)
                saveDataObjState()
                startActivity(intent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun saveDataObjState() {
        Log.d(TAG, "Inside SaveDataObjState")
        val start1 = integral1_start.value
        val start2 = integral2_start.value
        val start3 = integral3_start.value
        val end1 = integral1_end.value
        val end2 = integral2_end.value
        val end3 = integral3_end.value

        localDataObj?.setIntegralRanges(start1, start2, start3, end1, end2, end3)
        localDataObj?.updateTotalExpectedOperations()
    }

    companion object {
        const val TAG = "SettingsActivity"
    }
}
