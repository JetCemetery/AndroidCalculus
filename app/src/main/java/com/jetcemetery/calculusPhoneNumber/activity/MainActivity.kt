package com.jetcemetery.calculusPhoneNumber.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.jetcemetery.androidcalculus.R
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_MainLoop
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues
import com.jetcemetery.calculusPhoneNumber.helper.StartOperationHelper
import java.util.ArrayList

class MainActivity : AppCompatActivity(), CalcRecyclerViewAdapter.ItemClickListener {

    private var adapter: CalcRecyclerViewAdapter? = null

    private lateinit var txtPhone: EditText
    private lateinit var txtError: TextView
    private lateinit var txt_progressBar: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPauseStopLayout: View
    private lateinit var btnPause_Resume: MaterialButton
    private lateinit var btnStop: Button
    private lateinit var prbBar_progressBar: ProgressBar
    private var updateUIHandler: Handler? = null
    private var blockUpdates: Boolean = false

    private var local_TotalExpected: String? = null
    private var local_currentOperationsCompleted: Long = 0
    private var singleton_Thread: Singleton_MainLoop? = null
    private var localDataObj: Singleton_OperationValues? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Calling onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        txtPhone = findViewById(R.id.txt_phoneID)
        txtError = findViewById(R.id.errorText)
        btnStart = findViewById(R.id.btn_start)
        btnPause_Resume = findViewById(R.id.btn_PauseResume)
        btnPause_Resume.setText(R.string.strPause)
        btnStop = findViewById(R.id.btn_stop)
        btnPauseStopLayout = findViewById(R.id.layout_pause_stop)
        prbBar_progressBar = findViewById(R.id.pgBar_Progress_bar)
        txt_progressBar = findViewById(R.id.txt_progress)

        val tempDataFolder = ArrayList<String>()
        tempDataFolder.add("Results")

        val recyclerView = findViewById<RecyclerView>(R.id.resV_result)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CalcRecyclerViewAdapter(this, tempDataFolder)
        adapter?.setClickListener(this)
        recyclerView.adapter = adapter

        blockUpdates = false

        init_BothSingleTons()
        initView()
        createUpdateUiHandler()
        setUIData()
        safeResumeAllThreads()
        txtPhone.setText(localDataObj?.getPhoneNumber())
    }

    override fun onItemClick(view: View, position: Int) {
        val formula = adapter?.getItem(position) ?: return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Formula", formula)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Formula copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Calling onStart")
        init_BothSingleTons()
        safeResumeAllThreads()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Calling onResume")
        init_BothSingleTons()
        safeResumeAllThreads()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Calling onPause")
        safePauseAllThreads()
    }

    private fun init_BothSingleTons() {
        Log.d(TAG, "Inside init_BothSingleTons")
        Singleton_OperationValues.initInstance()
        localDataObj = Singleton_OperationValues.instance
        singleton_Thread = Singleton_MainLoop.instance
    }

    private fun initView() {
        Log.d(TAG, "Calling initView")
        txtError.visibility = View.GONE
        showStart_HidePauseStop(true)

        btnStart.setOnClickListener {
            txtError.visibility = View.GONE
            val dataObj = localDataObj ?: return@setOnClickListener
            val helperObj = StartOperationHelper(dataObj)
            if (singleton_Thread != null) {
                safeStopAllThreads()
            }
            if (helperObj.misconstruedIntegralRange()) {
                txtError.visibility = View.VISIBLE
                txtError.text = helperObj.misconstruedIntegralRange_text
                Toast.makeText(applicationContext, "Integral range is out of sequence", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (helperObj.numberInputValid(txtPhone)) {
                txtError.visibility = View.GONE
                initiateMainForLoopThread()
                lockStartButton()
            } else {
                var lettersFound = false
                var numberNotValid = false
                var numberCount = 0
                val rawNumbStr = txtPhone.text.toString()
                for (c in rawNumbStr.toCharArray()) {
                    if (c.isLetter()) {
                        lettersFound = true
                        break
                    }
                    if (c.isDigit()) {
                        numberCount++
                    }
                }
                if (numberCount <= 3) {
                    numberNotValid = true
                }
                var errorMsg = "Number Not valid!"
                if (lettersFound) {
                    errorMsg = "Number should not have any latin characters"
                } else if (numberNotValid) {
                    errorMsg = "Number is to small to be a valid phone number"
                }
                txtError.visibility = View.VISIBLE
                txtError.text = errorMsg
                Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        btnPause_Resume.setOnClickListener {
            val currentTxt = btnPause_Resume.text.toString()
            if (currentTxt.equals("Pause", ignoreCase = true)) {
                setPauseResumeButtonTo_Pause(false)
                safePauseAllThreads()
            } else {
                setPauseResumeButtonTo_Pause(true)
                safeResumeAllThreads()
            }
        }

        btnStop.setOnClickListener {
            Log.d(TAG, "about to call stop all threads because btnStop.setOnClickListener")
            safeStopAllThreads()
            resetDataObj()
        }
    }

    private fun setPauseResumeButtonTo_Pause(settingToPause: Boolean) {
        val myIcon: Drawable?
        if (settingToPause) {
            myIcon = ContextCompat.getDrawable(this, R.drawable.ic_pause)
            btnPause_Resume.text = "Pause"
        } else {
            myIcon = ContextCompat.getDrawable(this, R.drawable.ic_resume)
            btnPause_Resume.text = "Resume"
        }
        btnPause_Resume.icon = myIcon
    }

    private fun lockStartButton() {
        txtPhone.isEnabled = false
        txtPhone.setBackgroundColor(Color.GRAY)
    }

    private fun unlockStartButton() {
        txtPhone.isEnabled = true
        txtPhone.setBackgroundColor(Color.parseColor("#B7B773"))
    }

    private fun setUIData() {
        val dataObj = localDataObj ?: return
        if (dataObj.wasDataChanged()) {
            Log.d(TAG, "about to call stop all threads because localDataObj.wasDataChanged()")
            safeStopAllThreads()
            dataObj.clearChangesMadeLatch()
        }
        local_TotalExpected = dataObj.getTotalOperationExpected()
        local_currentOperationsCompleted = dataObj.getCurrentProgressCount()

        prbBar_progressBar.progress = dataObj.operationForProgressBar()
        val progressText = "$local_currentOperationsCompleted / $local_TotalExpected"
        txt_progressBar.text = progressText
        adapter?.clearData()
        adapter?.addDataList(dataObj.nu_GetDataList())
    }

    private fun resetDataObj() {
        Log.d(TAG, "Calling resetUIData")
        localDataObj?.setProgressCount(0)
        localDataObj?.setTextArea("")

        local_TotalExpected = localDataObj?.getTotalOperationExpected()
        local_currentOperationsCompleted = 0

        prbBar_progressBar.progress = 0
        val progressText = "0 / $local_TotalExpected"
        txt_progressBar.text = progressText
    }

    private fun initiateMainForLoopThread() {
        Log.d(TAG, "Calling initiateMainForLoopThread")
        adapter?.clearData()
        localDataObj?.setTextArea("")
        singleton_Thread?.stopAllThreads()
        singleton_Thread = null
        
        localDataObj?.setPhoneNumber(txtPhone.text.toString())
        Singleton_MainLoop.initInstance()
        singleton_Thread = Singleton_MainLoop.instance
        val handler = updateUIHandler ?: return
        val dataObj = localDataObj ?: return
        singleton_Thread?.MainForLoopThread(dataObj, handler)
        singleton_Thread?.StartProcess(dataObj)
        showStart_HidePauseStop(false)
    }

    private fun updateProgressBar_and_Text() {
        val dataObj = localDataObj ?: return
        if (!blockUpdates) {
            prbBar_progressBar.progress = dataObj.operationForProgressBar(local_currentOperationsCompleted.toFloat())
        }
        val progressTxt = "$local_currentOperationsCompleted / $local_TotalExpected"
        txt_progressBar.text = progressTxt
    }

    private fun showStart_HidePauseStop(showStart: Boolean) {
        Log.d(TAG, "Calling ShowStart_HidePauseStop")
        if (showStart) {
            btnPauseStopLayout.visibility = View.GONE
            btnStart.visibility = View.VISIBLE
        } else {
            btnPauseStopLayout.visibility = View.VISIBLE
            btnStart.visibility = View.GONE
        }
    }

    private fun safeStopAllThreads() {
        Log.d(TAG, "safe STOP all threads called")
        singleton_Thread?.let {
            it.stopAllThreads()
            singleton_Thread = null
            btnPause_Resume.setText(R.string.strPause)
            showStart_HidePauseStop(true)

            Thread {
                try {
                    Thread.sleep(50)
                    runOnUiThread { resetDataObj() }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }.start()
        }
        unlockStartButton()
        setPauseResumeButtonTo_Pause(true)
    }

    private fun safeResumeAllThreads() {
        Log.d(TAG, "safe RESUME all threads called")
        singleton_Thread?.let {
            val handler = updateUIHandler ?: return@let
            it.resumeAllThreads(handler)
            showStart_HidePauseStop(false)
            lockStartButton()
        }
        setPauseResumeButtonTo_Pause(true)
    }

    private fun safePauseAllThreads() {
        Log.d(TAG, "safe PAUSE all threads called")
        singleton_Thread?.pauseAllThreads()
        setPauseResumeButtonTo_Pause(false)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "calling onSaveInstanceState")
        super.onSaveInstanceState(savedInstanceState)
        safePauseAllThreads()
        saveDataObjState()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "calling onRestoreInstanceState")
        val tempTxt = localDataObj?.nu_GetDataList()
        super.onRestoreInstanceState(savedInstanceState)
        adapter?.clearData()
        if (tempTxt != null && tempTxt.isNotEmpty()) {
            adapter?.addDataList(tempTxt)
        }
        updateProgressBar_and_Text()
        safeResumeAllThreads()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == R.id.menu_home) {
                menu.getItem(i).isVisible = false
                break
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.menu_settings -> {
                Log.d(TAG, "Menu button hit, going to menu_settings")
                intent = Intent(applicationContext, SettingsActivity::class.java)
                saveDataObjState()
                startActivity(intent)
            }
            R.id.menu_about -> {
                Log.d(TAG, "Menu button hit, going to menu_about")
                intent = Intent(applicationContext, AboutActivity::class.java)
                saveDataObjState()
                startActivity(intent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun saveDataObjState() {
        Log.d(TAG, "calling SaveDataObjState")
        localDataObj?.setProgressCount(local_currentOperationsCompleted)
        adapter?.let { localDataObj?.nu_saveData(it.getList()) }
        val rawPhone = txtPhone.text.toString()
        localDataObj?.setPhoneNumber(rawPhone)
    }

    private fun createUpdateUiHandler() {
        if (updateUIHandler == null) {
            updateUIHandler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        POST_MESSAGE_IN_RESULTS -> {
                            val strMsg = msg.data.getString(MESSAGE_NAME_ID)
                            if (!strMsg.isNullOrEmpty()) {
                                val manyResults = strMsg.split("\n").toTypedArray()
                                adapter?.addManyResult(manyResults)
                            }
                        }
                        ONE_CALCULATION_COMPLETED -> {
                            local_currentOperationsCompleted++
                            updateProgressBar_and_Text()
                        }
                        ONE_CALCULATION_COMPLETED_BATCH -> {
                            val strMsg = msg.data.getString(MESSAGE_NAME_ID)
                            if (!strMsg.isNullOrEmpty()) {
                                local_currentOperationsCompleted += strMsg.toLong()
                                updateProgressBar_and_Text()
                            }
                        }
                        SUCCESSFUL_OPERATION -> {
                            Log.d(TAG, "messageTypeID == SUCCESSFUL_OPERATION")
                            safeStopAllThreads()
                            blockUpdates = true
                            prbBar_progressBar.progress = 100
                            singleton_Thread = null
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        const val POST_MESSAGE_IN_RESULTS = 555
        const val ONE_CALCULATION_COMPLETED_BATCH = 556
        const val ONE_CALCULATION_COMPLETED = 557
        const val SUCCESSFUL_OPERATION = 558
        const val MESSAGE_NAME_ID = "My_data_msg"
    }
}
