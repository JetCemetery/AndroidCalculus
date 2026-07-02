package com.jetcemetery.calculusPhoneNumber.activity

import android.os.Bundle
import android.widget.NumberPicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues
import com.jetcemetery.calculusPhoneNumber.helper.PreferencesManager

class SettingsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val dataObj = Singleton_OperationValues.instance
        
        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Settings", color = Color.Black) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.Black
                                    )
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
                if (dataObj != null) {
                    SettingsContent(
                        modifier = Modifier.padding(innerPadding),
                        dataObj = dataObj,
                        onSave = { s1, s2, s3, e1, e2, e3, cpu, stop ->
                            android.util.Log.d("SettingsActivity", "onSave clicked. Ranges: $s1-$e1, $s2-$e2, $s3-$e3")
                            dataObj.setIntegralRanges(s1, s2, s3, e1, e2, e3)
                            when (cpu) {
                                Singleton_OperationValues.CpuUseOptions.CPU_SINGLE -> dataObj.setCpu_single()
                                Singleton_OperationValues.CpuUseOptions.CPU_HALF -> dataObj.setCpu_half()
                                Singleton_OperationValues.CpuUseOptions.CPU_ALL_MINUS_1 -> dataObj.setCpu_all_m_1()
                                Singleton_OperationValues.CpuUseOptions.CPU_ALL -> dataObj.setCpu_all()
                            }
                            dataObj.setStopOnSuccess(stop)
                            dataObj.changesMade()

                            // Persist settings
                            PreferencesManager(this@SettingsActivity).saveSettings(
                                s1, s2, s3, e1, e2, e3, cpu, stop
                            )
                            finish()
                        }
                    )
                }
                }
            }
        }
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    dataObj: Singleton_OperationValues,
    onSave: (Int, Int, Int, Int, Int, Int, Singleton_OperationValues.CpuUseOptions, Boolean) -> Unit
) {
    var start1 by remember { mutableIntStateOf(dataObj.integral_1_Start()) }
    var start2 by remember { mutableIntStateOf(dataObj.integral_2_Start()) }
    var start3 by remember { mutableIntStateOf(dataObj.integral_3_Start()) }
    var end1 by remember { mutableIntStateOf(dataObj.integral_1_End()) }
    var end2 by remember { mutableIntStateOf(dataObj.integral_2_End()) }
    var end3 by remember { mutableIntStateOf(dataObj.integral_3_End()) }

    var selectedCpuOption by remember { mutableStateOf(dataObj.getCPU_OptionsEnum() ?: Singleton_OperationValues.CpuUseOptions.CPU_ALL) }
    var stopOnSuccess by remember { mutableStateOf(dataObj.getStopOnFirstSuccess()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (rest of the UI remains the same)
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(60.dp))
            HeaderCell("Integral 1", Modifier.weight(1f))
            HeaderCell("Integral 2", Modifier.weight(1f))
            HeaderCell("Integral 3", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelCell("Start", Modifier.width(60.dp))
            WheelPickerCell(start1, { start1 = it }, Modifier.weight(1f))
            WheelPickerCell(start2, { start2 = it }, Modifier.weight(1f))
            WheelPickerCell(start3, { start3 = it }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // End Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelCell("End", Modifier.width(60.dp))
            WheelPickerCell(end1, { end1 = it }, Modifier.weight(1f))
            WheelPickerCell(end2, { end2 = it }, Modifier.weight(1f))
            WheelPickerCell(end3, { end3 = it }, Modifier.weight(1f))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color.Black.copy(alpha = 0.2f))

        // Core Count
        Text("Core Count", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CpuOptionItem("One", Singleton_OperationValues.CpuUseOptions.CPU_SINGLE, selectedCpuOption) { selectedCpuOption = it }
            CpuOptionItem("Half", Singleton_OperationValues.CpuUseOptions.CPU_HALF, selectedCpuOption) { selectedCpuOption = it }
            CpuOptionItem("All-1", Singleton_OperationValues.CpuUseOptions.CPU_ALL_MINUS_1, selectedCpuOption) { selectedCpuOption = it }
            CpuOptionItem(
                label = "All",
                option = Singleton_OperationValues.CpuUseOptions.CPU_ALL,
                selectedOption = selectedCpuOption,
                isSpecial = true,
                onSelect = { selectedCpuOption = it }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color.Black.copy(alpha = 0.2f))

        // Stop on First Success
        Text("Stop on First Success", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = stopOnSuccess,
                onClick = { stopOnSuccess = true },
                colors = RadioButtonDefaults.colors(selectedColor = Color.Black)
            )
            Text("Yes", color = Color.Black, modifier = Modifier.clickable { stopOnSuccess = true })
            Spacer(modifier = Modifier.width(24.dp))
            RadioButton(
                selected = !stopOnSuccess,
                onClick = { stopOnSuccess = false },
                colors = RadioButtonDefaults.colors(selectedColor = Color.Black)
            )
            Text("No", color = Color.Black, modifier = Modifier.clickable { stopOnSuccess = false })
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onSave(start1, start2, start3, end1, end2, end3, selectedCpuOption, stopOnSuccess)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7B773))
        ) {
            Text("Save Settings", color = Color.Black)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CpuOptionItem(
    label: String,
    option: Singleton_OperationValues.CpuUseOptions,
    selectedOption: Singleton_OperationValues.CpuUseOptions,
    isSpecial: Boolean = false,
    onSelect: (Singleton_OperationValues.CpuUseOptions) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onSelect(option) }
            .padding(horizontal = 4.dp)
    ) {
        RadioButton(
            selected = (option == selectedOption),
            onClick = { onSelect(option) },
            colors = RadioButtonDefaults.colors(selectedColor = Color.Black)
        )
        Text(
            text = label,
            color = if (isSpecial) Color.Red else Color.Black,
            fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

@Composable
fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
fun LabelCell(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black, modifier = modifier)
}

@Composable
fun WheelPickerCell(value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = 1
                    maxValue = 500
                    this.value = value
                    setOnValueChangedListener { _, _, newVal ->
                        onValueChange(newVal)
                    }
                }
            },
            update = { picker ->
                picker.value = value
            }
        )
    }
}
