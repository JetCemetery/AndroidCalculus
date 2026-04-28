package com.jetcemetery.calculusPhoneNumber.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetcemetery.calculusPhoneNumber.R
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_MainLoop
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues

class NewMainActivity : ComponentActivity() {

    private var localDataObj: Singleton_OperationValues? = null
    private var singletonThread: Singleton_MainLoop? = null

    // Using a SnapshotStateList for automatic UI updates in Compose
    private val resultsList = mutableStateListOf<String>()
    
    // UI State
    private var isRunning by mutableStateOf(false)
    private var phoneNumber by mutableStateOf("")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        initData()
        
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    POST_MESSAGE_IN_RESULTS -> {
                        msg.data.getString(MESSAGE_NAME_ID)?.let {
                            resultsList.addAll(it.split("\n").filter { s -> s.isNotEmpty() })
                        }
                    }
                    SUCCESSFUL_OPERATION -> {
                        stopCalculation()
                    }
                }
            }
        }

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Calculus Phone Number", color = Color.Black) },
                            actions = {
                                IconButton(onClick = { 
                                    startActivity(Intent(this@NewMainActivity, AboutActivity::class.java))
                                }) {
                                    Icon(Icons.Default.Info, contentDescription = "About", tint = Color.Black)
                                }
                                IconButton(onClick = { 
                                    Toast.makeText(this@NewMainActivity, "Settings removed", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Black)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF7272B6)
                            )
                        )
                    },
                    containerColor = Color(0xFF7272B6),
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainContent(
                        modifier = Modifier.padding(innerPadding),
                        results = resultsList,
                        isRunning = isRunning,
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = { phoneNumber = it },
                        onStart = { startCalculation(handler) },
                        onPause = { pauseCalculation() },
                        onResume = { resumeCalculation(handler) },
                        onStop = { stopCalculation() }
                    )
                }
            }
        }
    }

    private fun initData() {
        Singleton_OperationValues.initInstance()
        localDataObj = Singleton_OperationValues.instance
        singletonThread = Singleton_MainLoop.instance
        
        localDataObj?.let {
            phoneNumber = it.getPhoneNumber() ?: ""
            resultsList.addAll(it.nu_GetDataList())
        }
    }

    private fun startCalculation(handler: Handler) {
        resultsList.clear()
        localDataObj?.apply {
            setTextArea("")
            setPhoneNumber(phoneNumber)
        }
        
        Singleton_MainLoop.initInstance()
        singletonThread = Singleton_MainLoop.instance
        
        val dataObj = localDataObj ?: return
        singletonThread?.MainForLoopThread(dataObj, handler)
        singletonThread?.StartProcess(dataObj)
        isRunning = true
    }

    private fun pauseCalculation() {
        singletonThread?.pauseAllThreads()
    }

    private fun resumeCalculation(handler: Handler) {
        singletonThread?.resumeAllThreads(handler)
    }

    private fun stopCalculation() {
        singletonThread?.stopAllThreads()
        singletonThread = null
        isRunning = false
    }

    companion object {
        const val POST_MESSAGE_IN_RESULTS = 555
        const val ONE_CALCULATION_COMPLETED_BATCH = 556
        const val ONE_CALCULATION_COMPLETED = 557
        const val SUCCESSFUL_OPERATION = 558
        const val MESSAGE_NAME_ID = "My_data_msg"
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    results: List<String>,
    isRunning: Boolean,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val context = LocalContext.current
    var isPaused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF7272B6)) // Using the project's background color
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Phone Number", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isRunning) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7B773))
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start", color = Color.Black)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (isPaused) onResume() else onPause()
                        isPaused = !isPaused
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7B773))
                ) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPaused) "Resume" else "Pause", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onStop()
                        isPaused = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7B773))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop", color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The LazyColumn (Modern replacement for RecyclerView)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            items(results) { item ->
                ResultItem(text = item, context = context)
            }
        }
    }
}

@Composable
fun ResultItem(text: String, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Calculus Result", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}
