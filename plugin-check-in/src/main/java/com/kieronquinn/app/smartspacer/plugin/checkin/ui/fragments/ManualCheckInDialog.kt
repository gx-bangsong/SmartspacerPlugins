package com.kieronquinn.app.smartspacer.plugin.checkin.ui.fragments

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
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualCheckInDialog(
    onDismiss: () -> Unit,
    onSave: (CheckInItem) -> Unit,
    checkInOnly: Boolean = false
) {
    val context = LocalContext.current
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    var dateStr by rememberSaveable { mutableStateOf(todayStr) }
    var checkInTimeStr by rememberSaveable { mutableStateOf("09:00") }
    var checkOutTimeStr by rememberSaveable { mutableStateOf("18:00") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("手动添加打卡记录") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            if (dateStr.isBlank()) {
                                Toast.makeText(context, "日期不能为空", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            val datePattern = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            try {
                                datePattern.parse(dateStr.trim())
                            } catch (e: Exception) {
                                Toast.makeText(context, "日期格式错误，请输入 yyyy-MM-dd", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            val sdfFull = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                            val checkInTimeMs = if (checkInTimeStr.isNotBlank()) {
                                try {
                                    sdfFull.parse("${dateStr.trim()} ${checkInTimeStr.trim()}")?.time
                                } catch (e: Exception) {
                                    Toast.makeText(context, "上班打卡时间格式错误，请输入 HH:mm", Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }
                            } else null

                            val checkOutTimeMs = if (checkInOnly) {
                                // 仅上班打卡模式下不记录下班时间
                                null
                            } else if (checkOutTimeStr.isNotBlank()) {
                                try {
                                    sdfFull.parse("${dateStr.trim()} ${checkOutTimeStr.trim()}")?.time
                                } catch (e: Exception) {
                                    Toast.makeText(context, "下班打卡时间格式错误，请输入 HH:mm", Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }
                            } else null

                            val item = CheckInItem(
                                date = dateStr.trim(),
                                checkInTime = checkInTimeMs,
                                checkOutTime = checkOutTimeMs
                            )
                            onSave(item)
                            Toast.makeText(context, "打卡记录已成功添加", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("保存")
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
                Text("输入打卡信息：", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("日期 (格式: yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = checkInTimeStr,
                    onValueChange = { checkInTimeStr = it },
                    label = { Text("上班时间 (格式: HH:mm，留空代表未打卡)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (!checkInOnly) {
                    OutlinedTextField(
                        value = checkOutTimeStr,
                        onValueChange = { checkOutTimeStr = it },
                        label = { Text("下班时间 (格式: HH:mm，留空代表未打卡)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
