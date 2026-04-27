package com.jetcemetery.calculusPhoneNumber.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.*
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetcemetery.androidcalculus.databinding.ActivityMainBinding
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_MainLoop
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues
import com.jetcemetery.calculusPhoneNumber.helper.StartOperationHelper

class MainActivity : AppCompatActivity(), CalcRecyclerViewAdapter.ItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private var adapter: CalcRecyclerViewAdapter? = null
    private var updateUIHandler: Handler? = null
    
    private var localDataObj: Singleton_OperationValues? = null
    private var singletonThread: Singleton_MainLoop? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        setupUI()
        initData()
        createUpdateUiHandler()
    }

    private fun setupUI() {
        // Setup RecyclerView
        adapter = CalcRecyclerViewAdapter(this, ArrayList()).apply {
            setClickListener(this@MainActivity)
        }
        binding.recyclerView?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        // Start Button Logic
        binding.btnStart?.setOnClickListener {
            if (validateInput()) {
                startCalculation()
                toggleButtons(isRunning = true)
            }
        }

        // Pause Button Logic
        binding.btnPause?.setOnClickListener {
            val isPaused = binding.btnPause?.text.toString().equals("Resume", ignoreCase = true)
            if (isPaused) {
                resumeCalculation()
            } else {
                pauseCalculation()
            }
        }

        // Stop Button Logic
        binding.btnStop?.setOnClickListener {
            stopCalculation()
            toggleButtons(isRunning = false)
        }
    }

    private fun validateInput(): Boolean {
        val phoneTxt = binding.txtPhone ?: return false
        val dataObj = localDataObj ?: return false
        val helper = StartOperationHelper(dataObj)
        
        return if (helper.numberInputValid(phoneTxt)) {
            true
        } else {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun startCalculation() {
        adapter?.clearData()
        val phoneNumber = binding.txtPhone?.text.toString()
        localDataObj?.setPhoneNumber(phoneNumber)
        
        Singleton_MainLoop.initInstance()
        singletonThread = Singleton_MainLoop.instance
        
        val handler = updateUIHandler ?: return
        val dataObj = localDataObj ?: return
        
        singletonThread?.MainForLoopThread(dataObj, handler)
        singletonThread?.StartProcess(dataObj)
    }

    private fun pauseCalculation() {
        singletonThread?.pauseAllThreads()
        binding.btnPause?.text = "Resume"
    }

    private fun resumeCalculation() {
        val handler = updateUIHandler ?: return
        singletonThread?.resumeAllThreads(handler)
        binding.btnPause?.text = "Pause"
    }

    private fun stopCalculation() {
        singletonThread?.stopAllThreads()
        singletonThread = null
        binding.btnPause?.text = "Pause"
    }

    private fun toggleButtons(isRunning: Boolean) {
        if (isRunning) {
            binding.btnStart?.visibility = View.GONE
            binding.layoutRunning?.visibility = View.VISIBLE
            binding.txtPhone?.isEnabled = false
        } else {
            binding.btnStart?.visibility = View.VISIBLE
            binding.layoutRunning?.visibility = View.GONE
            binding.txtPhone?.isEnabled = true
        }
    }

    private fun initData() {
        Singleton_OperationValues.initInstance()
        localDataObj = Singleton_OperationValues.instance
        singletonThread = Singleton_MainLoop.instance
        
        localDataObj?.let {
            binding.txtPhone?.setText(it.getPhoneNumber())
            adapter?.addDataList(it.nu_GetDataList())
        }
    }

    private fun createUpdateUiHandler() {
        updateUIHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    POST_MESSAGE_IN_RESULTS -> {
                        msg.data.getString(MESSAGE_NAME_ID)?.let {
                            adapter?.addManyResult(it.split("\n").toTypedArray())
                        }
                    }
                    SUCCESSFUL_OPERATION -> {
                        stopCalculation()
                        toggleButtons(isRunning = false)
                        Toast.makeText(this@MainActivity, "Calculation Complete!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onItemClick(view: View, position: Int) {
        val formula = adapter?.getItem(position) ?: return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Formula", formula))
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val POST_MESSAGE_IN_RESULTS = 555
        const val ONE_CALCULATION_COMPLETED_BATCH = 556
        const val ONE_CALCULATION_COMPLETED = 557
        const val SUCCESSFUL_OPERATION = 558
        const val MESSAGE_NAME_ID = "My_data_msg"
    }
}
