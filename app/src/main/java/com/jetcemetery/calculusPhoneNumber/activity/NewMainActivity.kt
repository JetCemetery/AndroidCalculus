package com.jetcemetery.calculusPhoneNumber.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_MainLoop
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues
import com.jetcemetery.calculusPhoneNumber.helper.PreferencesManager

class NewMainActivity : ComponentActivity() {

    private var localDataObj: Singleton_OperationValues? = null
    private var singletonThread: Singleton_MainLoop? = null

    // Using a SnapshotStateList for automatic UI updates in Compose
    private val resultsList = mutableStateListOf<String>()
    
    // UI State
    private var isRunning by mutableStateOf(false)
    private var phoneNumber by mutableStateOf("")
    private var currentProgress by mutableFloatStateOf(0f)
    private var progressText by mutableStateOf("")

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
                    ONE_CALCULATION_COMPLETED_BATCH -> {
                        msg.data.getString(MESSAGE_NAME_ID)?.let {
                            val completedInBatch = it.toLongOrNull() ?: 0L
                            localDataObj?.let { data ->
                                data.setProgressCount(data.getCurrentProgressCount() + completedInBatch)
                                currentProgress = data.operationForProgressBar() / 100f
                                progressText = "${data.getCurrentProgressCount()} / ${data.getTotalOperationExpected()}"
                            }
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
                                    startActivity(Intent(this@NewMainActivity, SettingsActivity::class.java))
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
                        currentProgress = currentProgress,
                        progressText = progressText,
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
            PreferencesManager(this).loadSettings(it)
            phoneNumber = it.getPhoneNumber() ?: ""
            resultsList.addAll(it.nu_GetDataList())
            currentProgress = it.operationForProgressBar() / 100f
            progressText = "${it.getCurrentProgressCount()} / ${it.getTotalOperationExpected()}"
        }
    }

    private fun startCalculation(handler: Handler) {
        resultsList.clear()
        currentProgress = 0f
        progressText = ""
        localDataObj?.apply {
            setTextArea("")
            setPhoneNumber(phoneNumber)
            setProgressCount(0)
            updateTotalExpectedOperations()
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
    currentProgress: Float,
    progressText: String,
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

        if (isRunning) {
            LinearProgressIndicator(
                progress = { currentProgress },
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
            Text(
                text = progressText,
                color = Color.Black,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

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
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
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
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF7272B6))
                .scrollbar(state = listState, color = Color.Black.copy(alpha = 0.5f))
        ) {
            items(results) { item ->
                ResultItem(text = item, context = context)
            }
        }
    }
}

@Composable
fun Modifier.scrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    color: Color
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "scrollbarAlpha"
    )

    return drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        val totalItemsCount = layoutInfo.totalItemsCount

        if (visibleItems.isNotEmpty() && totalItemsCount > visibleItems.size) {
            val firstVisibleItem = visibleItems.first()
            val viewportHeight = size.height
            
            val scrollbarHeight = (visibleItems.size.toFloat() / totalItemsCount) * viewportHeight
            val scrollbarOffsetY = (firstVisibleItem.index.toFloat() / totalItemsCount) * viewportHeight

            drawRect(
                color = color,
                topLeft = Offset(size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultItem(text: String, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    
                    // Copy all text as requested
                    val clip = ClipData.newPlainText("Calculus Result", text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied all text", Toast.LENGTH_SHORT).show()
                },
                onDoubleClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    
                    // Copy all text as requested
                    val clip = ClipData.newPlainText("Full Calculus Result", text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied all text", Toast.LENGTH_SHORT).show()
                }
            ),
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
