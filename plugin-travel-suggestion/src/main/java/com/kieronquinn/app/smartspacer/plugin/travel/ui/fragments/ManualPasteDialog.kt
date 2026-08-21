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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kieronquinn.app.smartspacer.plugin.travel.R
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.shared.smsparser.ParseResultStatus
import com.kieronquinn.app.smartspacer.shared.smsparser.SmsParser
import com.kieronquinn.app.smartspacer.shared.smsparser.TravelInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The "paste → parse → editable review → save" dialog, shared by the settings screen and the
 * sharesheet flow (via [ManualPasteFragment]). When [initialInfo] is provided the raw-paste step
 * is skipped and the form opens directly in the editable review state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualPasteDialog(
    initialInfo: TravelInfo? = null,
    source: String = "manual",
    onDismiss: () -> Unit,
    onSave: (TravelInfoItem) -> Unit
) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    var rawText by rememberSaveable { mutableStateOf("") }

    var isParsed by rememberSaveable { mutableStateOf(initialInfo != null) }
    var trainNumber by rememberSaveable { mutableStateOf(initialInfo?.trainNumber ?: "") }
    var departureStation by rememberSaveable { mutableStateOf(initialInfo?.departureStation ?: "") }
    var arrivalStation by rememberSaveable { mutableStateOf(initialInfo?.arrivalStation ?: "") }
    var departureTimeStr by rememberSaveable {
        mutableStateOf(
            initialInfo?.let { timeFormat.format(Date(it.departureTime)) } ?: ""
        )
    }
    var seat by rememberSaveable { mutableStateOf(initialInfo?.seat ?: "") }
    var passengerName by rememberSaveable { mutableStateOf(initialInfo?.passengerName ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.manual_paste_title)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.manual_paste_cancel))
                        }
                    },
                    actions = {
                        if (isParsed) {
                            TextButton(onClick = {
                                if (trainNumber.isBlank() || departureStation.isBlank() || departureTimeStr.isBlank()) {
                                    Toast.makeText(context, context.getString(R.string.manual_paste_required), Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }
                                val parsedTimeMs = try {
                                    timeFormat.parse(departureTimeStr.trim())?.time ?: System.currentTimeMillis()
                                } catch (e: Exception) {
                                    Toast.makeText(context, context.getString(R.string.manual_paste_time_format_error), Toast.LENGTH_SHORT).show()
                                    return@TextButton
                                }
                                val item = TravelInfoItem(
                                    trainNumber = trainNumber.trim(),
                                    departureStation = departureStation.trim(),
                                    arrivalStation = arrivalStation.trim().ifBlank { null },
                                    departureTime = parsedTimeMs,
                                    seat = seat.trim().ifBlank { null },
                                    passengerName = passengerName.trim().ifBlank { null },
                                    source = source
                                )
                                onSave(item)
                                Toast.makeText(context, context.getString(R.string.manual_paste_saved), Toast.LENGTH_SHORT).show()
                            }) {
                                Text(stringResource(R.string.manual_paste_save))
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
                    Text(stringResource(R.string.manual_paste_hint), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        label = { Text(stringResource(R.string.manual_paste_label)) },
                        placeholder = { Text(stringResource(R.string.manual_paste_placeholder)) },
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
                                departureTimeStr = timeFormat.format(Date(info.departureTime))
                                seat = info.seat ?: ""
                                passengerName = info.passengerName ?: ""
                                isParsed = true
                                Toast.makeText(context, context.getString(R.string.manual_paste_parse_success), Toast.LENGTH_SHORT).show()
                            } else {
                                val errorMsg = result.errorMessage ?: context.getString(R.string.manual_paste_unknown_error)
                                Toast.makeText(context, context.getString(R.string.manual_paste_parse_failed, errorMsg), Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.manual_paste_parse))
                    }
                } else {
                    Text(stringResource(R.string.manual_paste_review_title), style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = trainNumber,
                        onValueChange = { trainNumber = it },
                        label = { Text(stringResource(R.string.manual_paste_field_train)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = departureStation,
                        onValueChange = { departureStation = it },
                        label = { Text(stringResource(R.string.manual_paste_field_from)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = arrivalStation,
                        onValueChange = { arrivalStation = it },
                        label = { Text(stringResource(R.string.manual_paste_field_to)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = departureTimeStr,
                        onValueChange = { departureTimeStr = it },
                        label = { Text(stringResource(R.string.manual_paste_field_time)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = seat,
                        onValueChange = { seat = it },
                        label = { Text(stringResource(R.string.manual_paste_field_seat)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passengerName,
                        onValueChange = { passengerName = it },
                        label = { Text(stringResource(R.string.manual_paste_field_passenger)) },
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
                            Text(stringResource(R.string.manual_paste_repaste))
                        }
                    }
                }
            }
        }
    }
}
