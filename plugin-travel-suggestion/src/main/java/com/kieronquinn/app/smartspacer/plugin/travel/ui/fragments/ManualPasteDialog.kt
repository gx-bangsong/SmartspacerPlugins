package com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.shared.smsparser.SmsParser
import com.kieronquinn.app.smartspacer.shared.smsparser.ParseResultStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualPasteDialog(
    onDismiss: () -> Unit,
    onSave: (TravelInfoItem) -> Unit
) {
    val context = LocalContext.current
    var rawText by rememberSaveable { mutableStateOf("") }

    var isParsed by rememberSaveable { mutableStateOf(false) }
    var trainNumber by rememberSaveable { mutableStateOf("") }
    var departureStation by rememberSaveable { mutableStateOf("") }
    var arrivalStation by rememberSaveable { mutableStateOf("") }
    var departureTimeStr by rememberSaveable { mutableStateOf("") }
    var seat by rememberSaveable { mutableStateOf("") }
    var passengerName by rememberSaveable { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("手动粘贴解析行程") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        if (isParsed) {
                            TextButton(onClick = {
                                if (trainNumber.isBlank() || departureStation.isBlank() || departureTimeStr.isBlank()) {
                                    Toast.makeText(context, "车次、出发地及出发时间不能为空", Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                val parsedTimeMs = try {
                                    sdf.parse(departureTimeStr.trim())?.time ?: System.currentTimeMillis()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "时间格式错误，请输入: yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }
                                val item = TravelInfoItem(
                                    trainNumber = trainNumber.trim(),
                                    departureStation = departureStation.trim(),
                                    arrivalStation = arrivalStation.trim().ifBlank { null },
                                    departureTime = parsedTimeMs,
                                    seat = seat.trim().ifBlank { null },
                                    passengerName = passengerName.trim().ifBlank { null },
                                    source = "manual"
                                )
                                onSave(item)
                                Toast.makeText(context, "行程已成功保存", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("保存")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isParsed) {
                    Text("请粘贴购票或出票短信：", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        label = { Text("短信或文本内容") },
                        placeholder = { Text("【12306】...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                    Button(
                        onClick = {
                            val parser = SmsParser(context)
                            val result = parser.parseTravelInfo(rawText)
                            if (result.status == ParseResultStatus.SUCCESS && result.travelInfo != null) {
                                val info = result.travelInfo!!
                                trainNumber = info.trainNumber
                                departureStation = info.departureStation
                                arrivalStation = info.arrivalStation ?: ""
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                departureTimeStr = sdf.format(Date(info.departureTime))
                                seat = info.seat ?: ""
                                passengerName = info.passengerName ?: ""
                                isParsed = true
                                Toast.makeText(context, "解析成功，请核对信息后保存！", Toast.LENGTH_SHORT).show()
                            } else {
                                val errorMsg = result.errorMessage ?: "未知错误"
                                Toast.makeText(context, "解析失败: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("解析文本")
                    }
                } else {
                    Text("核对解析到的行程信息（可编辑）：", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = trainNumber,
                        onValueChange = { trainNumber = it },
                        label = { Text("车次 / 航班号") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = departureStation,
                        onValueChange = { departureStation = it },
                        label = { Text("出发站 / 机场") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = arrivalStation,
                        onValueChange = { arrivalStation = it },
                        label = { Text("到达站 / 机场（选填）") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = departureTimeStr,
                        onValueChange = { departureTimeStr = it },
                        label = { Text("出发时间 (格式: yyyy-MM-dd HH:mm)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = seat,
                        onValueChange = { seat = it },
                        label = { Text("座位号（选填）") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passengerName,
                        onValueChange = { passengerName = it },
                        label = { Text("乘车人姓名（选填）") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isParsed = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("重新粘贴")
                        }
                    }
                }
            }
        }
    }
}
