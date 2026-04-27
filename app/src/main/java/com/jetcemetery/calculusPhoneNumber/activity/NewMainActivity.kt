package com.jetcemetery.calculusPhoneNumber.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetcemetery.androidcalculus.R
import com.jetcemetery.androidcalculus.databinding.ActivityNewMainBinding
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_MainLoop
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues
import com.jetcemetery.calculusPhoneNumber.helper.StartOperationHelper

class NewMainActivity : AppCompatActivity(), CalcRecyclerViewAdapter.ItemClickListener {

    private lateinit var binding: ActivityNewMainBinding
    private var adapter: CalcRecyclerViewAdapter? = null
    private var updateUIHandler: Handler? = null
    
    private var localDataObj: Singleton_OperationValues? = null
    private var singletonThread: Singleton_MainLoop? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityNewMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make the view aware of system bars (ActionBar/Status Bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)

        setupUI()
        initData()
        createUpdateUiHandler()
    }

    private fun setupUI() {
        adapter = CalcRecyclerViewAdapter(this, ArrayList()).apply {
            setClickListener(this@NewMainActivity)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@NewMainActivity)
            adapter = this@NewMainActivity.adapter
        }

        binding.btnStart.setOnClickListener {
            if (validateInput()) {
                startCalculation()
                toggleUiState(isRunning = true)
            }
        }

        binding.btnPause.setOnClickListener {
            val isPaused = binding.btnPause.text.toString().equals("Resume", ignoreCase = true)
            if (isPaused) {
                resumeCalculation()
            } else {
                pauseCalculation()
            }
        }

        binding.btnStop.setOnClickListener {
            stopCalculation()
            toggleUiState(isRunning = false)
        }
    }

    private fun validateInput(): Boolean {
        val helper = StartOperationHelper(localDataObj ?: return false)
        return if (helper.numberInputValid(binding.txtPhone)) {
            true
        } else {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun startCalculation() {
        adapter?.clearData()
        localDataObj?.apply {
            setTextArea("")
            setPhoneNumber(binding.txtPhone.text.toString())
        }
        
        Singleton_MainLoop.initInstance()
        singletonThread = Singleton_MainLoop.instance
        
        val handler = updateUIHandler ?: return
        val dataObj = localDataObj ?: return
        
        singletonThread?.MainForLoopThread(dataObj, handler)
        singletonThread?.StartProcess(dataObj)
    }

    private fun pauseCalculation() {
        singletonThread?.pauseAllThreads()
        binding.btnPause.text = "Resume"
        binding.btnPause.setIconResource(R.drawable.ic_resume)
    }

    private fun resumeCalculation() {
        val handler = updateUIHandler ?: return
        singletonThread?.resumeAllThreads(handler)
        binding.btnPause.text = getString(R.string.strPause)
        binding.btnPause.setIconResource(R.drawable.ic_pause)
    }

    private fun stopCalculation() {
        singletonThread?.stopAllThreads()
        singletonThread = null
        binding.btnPause.text = getString(R.string.strPause)
        binding.btnPause.setIconResource(R.drawable.ic_pause)
    }

    private fun toggleUiState(isRunning: Boolean) {
        binding.btnStart.visibility = if (isRunning) View.GONE else View.VISIBLE
        binding.layoutRunning.visibility = if (isRunning) View.VISIBLE else View.GONE
        binding.txtPhone.isEnabled = !isRunning
    }

    private fun initData() {
        Singleton_OperationValues.initInstance()
        localDataObj = Singleton_OperationValues.instance
        singletonThread = Singleton_MainLoop.instance
        
        localDataObj?.let {
            binding.txtPhone.setText(it.getPhoneNumber())
            adapter?.addDataList(it.nu_GetDataList())
        }
    }

    private fun createUpdateUiHandler() {
        updateUIHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MainActivity.POST_MESSAGE_IN_RESULTS -> {
                        msg.data.getString(MainActivity.MESSAGE_NAME_ID)?.let {
                            adapter?.addManyResult(it.split("\n").toTypedArray())
                        }
                    }
                    MainActivity.SUCCESSFUL_OPERATION -> {
                        stopCalculation()
                        toggleUiState(isRunning = false)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
